import asyncio
import logging

from aiogram import Bot, Dispatcher, F
from aiogram.client.bot import DefaultBotProperties
from aiogram.enums import ParseMode
from aiogram.filters import Command, CommandStart
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup
from aiogram.types import CallbackQuery, InlineKeyboardButton, InlineKeyboardMarkup, BufferedInputFile, Message

from quiz_logic import QuizConfig, QuizResult, load_quiz_config, score_for_question_choice
from telegram_images import build_fallback_result_image, try_send_photo_from_url
from utils import get_bot_start_url, get_env, safe_json_dumps, build_tg_share_url


logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("bot")


class QuizFSM(StatesGroup):
    in_quiz = State()
    waiting_feedback = State()


def build_answer_keyboard(config: QuizConfig, question_index: int) -> InlineKeyboardMarkup:
    q = config.questions[question_index]
    rows = [
        [InlineKeyboardButton(text=opt.text, callback_data=f"q:{question_index}:a:{opt.id}")]
        for opt in q.options
    ]
    return InlineKeyboardMarkup(inline_keyboard=rows)


async def cmd_start(message: Message, state: FSMContext, bot: Bot, config: QuizConfig):
    await state.clear()
    await state.set_state(QuizFSM.in_quiz)

    start_question = config.questions[0]
    await state.update_data(current_index=0, score={}, final_result=None)

    await message.answer("Привет! Я помогу узнать твоё тотемное животное в Московском зоопарке.")
    await message.answer(start_question.text)
    await message.answer(
        start_question.caption or "Выберите вариант:",
        reply_markup=build_answer_keyboard(config, 0),
    )


async def on_choice(callback: CallbackQuery, state: FSMContext, bot: Bot, config: QuizConfig):
    data = callback.data or ""
    try:
        _, q_index_s, _, option_id = data.split(":", 3)
        q_index = int(q_index_s)
    except Exception:
        await callback.answer("Ошибка формата выбора. Попробуйте еще раз.")
        return

    stored = await state.get_data()
    current_index = int(stored.get("current_index", 0))
    if q_index != current_index:
        await callback.answer("Похоже, этот вопрос уже пройден.")
        await callback.message.answer(
            "Хочешь пройти викторину заново?",
            reply_markup=InlineKeyboardMarkup(
                inline_keyboard=[[InlineKeyboardButton(text="Попробовать ещё раз?", callback_data="restart")]]
            ),
        )
        return

    question = config.questions[q_index]
    option = next((o for o in question.options if o.id == option_id), None)
    if not option:
        await callback.answer("Неизвестный вариант. Попробуй ещё раз.")
        return

    score: dict[str, int] = stored.get("score", {})
    new_score = score_for_question_choice(score, option.scores)

    next_index = current_index + 1
    if next_index >= len(config.questions):
        result = QuizResult.from_score(config, new_score)
        await state.update_data(final_result=result.to_dict())
        await show_result(callback, state, bot, config, result)
        return

    await state.update_data(score=new_score, current_index=next_index)
    next_question = config.questions[next_index]
    await callback.message.answer(next_question.text)
    await callback.message.answer(
        next_question.caption or "Выберите вариант:",
        reply_markup=build_answer_keyboard(config, next_index),
    )
    await callback.answer()


async def show_result(callback: CallbackQuery, state: FSMContext, bot: Bot, config: QuizConfig, result: QuizResult):
    await callback.answer()
    share_url = await get_bot_start_url(bot)

    animals_caption = (
        f"<b>Твоё тотемное животное в Московском зоопарке</b>: {result.animal.name}\n\n"
        f"{result.animal.description}\n\n"
        f"Интересный факт: {result.animal.fun_fact}\n"
    )

    photo_sent = await try_send_photo_from_url(
        bot=bot,
        chat_id=callback.message.chat.id,
        url=result.animal.image_url,
        caption=animals_caption,
    )
    if not photo_sent:
        img_bytes = build_fallback_result_image(result.animal.name)
        await bot.send_photo(
            chat_id=callback.message.chat.id,
            photo=BufferedInputFile(img_bytes.getvalue(), filename="result.png"),
            caption=animals_caption,
        )

    adoption_text = (
        "<b>Программа опеки</b> помогает поддерживать заботу о животных.\n\n"
        f"Твой результат — <b>{result.animal.name}</b>.\n"
        "Нажми кнопку и мы подскажем, как стать опекуном."
    )

    await bot.send_message(
        chat_id=callback.message.chat.id,
        text=adoption_text,
        parse_mode=ParseMode.HTML,
        reply_markup=InlineKeyboardMarkup(
            inline_keyboard=[
                [InlineKeyboardButton(text="Узнать больше о животных", url="https://moscowzoo.ru/animals/kinds")],
                [InlineKeyboardButton(text="Связаться с сотрудником", callback_data="contact")],
            ]
        ),
    )

    await bot.send_message(
        chat_id=callback.message.chat.id,
        text="Можно поделиться результатом и попробовать ещё раз!",
        reply_markup=InlineKeyboardMarkup(
            inline_keyboard=[
                [InlineKeyboardButton(text="Поделиться", callback_data="share")],
                [InlineKeyboardButton(text="Попробовать ещё раз?", callback_data="restart")],
                [InlineKeyboardButton(text="Оставить отзыв", callback_data="feedback")],
            ]
        ),
    )


async def on_share(callback: CallbackQuery, state: FSMContext, bot: Bot, config: QuizConfig):
    stored = await state.get_data()
    final = stored.get("final_result")
    if not final:
        await callback.answer("Сначала пройдите викторину.")
        return

    share_text = (
        f"Твоё тотемное животное в Московском зоопарке: {final.get('animal_name')}\n\n"
        "Пройди викторину и узнай своё!"
    )
    share_link = await get_bot_start_url(bot)
    tg_share_url = build_tg_share_url(url=share_link, text=share_text)

    await callback.message.answer(
        "Нажми кнопку ниже, чтобы поделиться результатом (текст будет содержать ссылку на бота).",
        reply_markup=InlineKeyboardMarkup(
            inline_keyboard=[[InlineKeyboardButton(text="Поделиться в Telegram", url=tg_share_url)]]
        ),
    )
    await callback.answer()


async def on_contact(callback: CallbackQuery, state: FSMContext, bot: Bot, config: QuizConfig):
    stored = await state.get_data()
    final = stored.get("final_result")
    if not final:
        await callback.answer("Сначала пройдите викторину.")
        return

    admin_id = get_env("ADMIN_TELEGRAM_ID", "")
    contact = get_env("ADOPTION_CONTACT", "")
    if admin_id.isdigit():
        try:
            await bot.send_message(
                chat_id=int(admin_id),
                text="Пользователь просит связаться.\n\n"
                f"Результат: {safe_json_dumps(final)}",
            )
        except Exception:
            logger.exception("Failed to notify admin")

    await callback.message.answer(
        f"Напишите сотруднику: {contact or 'контакт не настроен'}\n\n"
        "Мы также переслали результат сотруднику, чтобы он мог быстрее ответить."
    )
    await callback.answer()


async def on_restart(callback: CallbackQuery, state: FSMContext, bot: Bot, config: QuizConfig):
    await cmd_start(callback.message, state, bot, config)
    await callback.answer()


async def on_feedback(callback: CallbackQuery, state: FSMContext, bot: Bot):
    await state.set_state(QuizFSM.waiting_feedback)
    await callback.message.answer("Оставьте отзыв: напишите, что понравилось/что улучшить.")
    await callback.answer()


async def on_feedback_text(message: Message, state: FSMContext, bot: Bot):
    feedback = (message.text or "").strip()
    if not feedback:
        await message.answer("Похоже, отзыв пустой. Напишите пару предложений.")
        return

    stored = await state.get_data()
    final = stored.get("final_result")
    admin_id = get_env("ADMIN_TELEGRAM_ID", "")
    if admin_id.isdigit():
        try:
            await bot.send_message(
                chat_id=int(admin_id),
                text="Новый отзыв пользователя.\n\n"
                f"Отзыв: {feedback}\n\n"
                f"Результат: {safe_json_dumps(final)}",
            )
        except Exception:
            logger.exception("Failed to send feedback to admin")

    await message.answer("Спасибо за отзыв! Мы учтем предложения.")
    await state.clear()


async def main():
    config = load_quiz_config()
    bot_token = get_env("BOT_TOKEN", "")
    if not bot_token or "PUT_YOUR_TELEGRAM_BOT_TOKEN_HERE" in bot_token:
        logger.warning("BOT_TOKEN не задан. Укажите его в .env перед запуском.")

    bot = Bot(token=bot_token, default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    dp = Dispatcher()

    async def start_handler(message: Message, state: FSMContext):
        await cmd_start(message, state, bot, config)

    async def choice_handler(callback: CallbackQuery, state: FSMContext):
        await on_choice(callback, state, bot, config)

    async def contact_handler(callback: CallbackQuery, state: FSMContext):
        await on_contact(callback, state, bot, config)

    async def restart_handler(callback: CallbackQuery, state: FSMContext):
        await on_restart(callback, state, bot, config)

    async def feedback_handler(callback: CallbackQuery, state: FSMContext):
        await on_feedback(callback, state, bot)

    async def share_handler(callback: CallbackQuery, state: FSMContext):
        await on_share(callback, state, bot, config)

    async def feedback_text_handler(message: Message, state: FSMContext):
        await on_feedback_text(message, state, bot)

    async def on_error(*args, **kwargs):
        exc = None
        for a in args:
            if isinstance(a, BaseException):
                exc = a
                break
        if exc is None and isinstance(kwargs.get("exception"), BaseException):
            exc = kwargs["exception"]
        if exc:
            logger.error("Unhandled error: %r", exc, exc_info=(type(exc), exc, exc.__traceback__))
        else:
            logger.exception("Unhandled error")

    dp.message.register(start_handler, CommandStart())
    dp.message.register(start_handler, Command("start"))

    dp.callback_query.register(choice_handler, F.data.startswith("q:"))
    dp.callback_query.register(contact_handler, F.data == "contact")
    dp.callback_query.register(restart_handler, F.data == "restart")
    dp.callback_query.register(feedback_handler, F.data == "feedback")
    dp.callback_query.register(share_handler, F.data == "share")

    dp.message.register(feedback_text_handler, QuizFSM.waiting_feedback)
    dp.errors.register(on_error)

    await dp.start_polling(bot)


if __name__ == "__main__":
    import dotenv

    dotenv.load_dotenv()
    asyncio.run(main())
