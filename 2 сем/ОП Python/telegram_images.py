import io
import logging
from typing import Optional

import aiohttp
from aiogram.types import BufferedInputFile
from PIL import Image, ImageDraw, ImageFont

logger = logging.getLogger("images")


async def try_send_photo_from_url(*, bot, chat_id: int, url: Optional[str], caption: str) -> bool:
    if not url:
        return False
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url) as resp:
                if resp.status != 200:
                    return False
                data = await resp.read()

        # aiogram 3.x ожидает InputFile-подобный объект, поэтому используем BufferedInputFile.
        filename = url.split("?")[0].rstrip("/").split("/")[-1] or "photo"
        photo = BufferedInputFile(data, filename=filename)
        await bot.send_photo(chat_id=chat_id, photo=photo, caption=caption)
        return True
    except Exception:
        logger.exception("Failed to send photo from url")
        return False


def build_fallback_result_image(animal_name: str) -> io.BytesIO:
    w, h = 1024, 576
    img = Image.new("RGB", (w, h), (14, 26, 50))
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("arial.ttf", 72)
        font_small = ImageFont.truetype("arial.ttf", 34)
    except Exception:
        font_title = ImageFont.load_default()
        font_small = ImageFont.load_default()

    draw.text((60, 80), "Твоё тотемное животное", fill=(255, 255, 255), font=font_title)
    draw.text((60, 190), animal_name, fill=(120, 230, 255), font=font_title)
    draw.text(
        (60, 330),
        "Отвечай, делись результатом и попробуй ещё раз!",
        fill=(220, 230, 240),
        font=font_small,
    )

    draw.ellipse((780, 60, 1000, 280), fill=(40, 160, 255))
    draw.ellipse((700, 320, 960, 540), fill=(255, 150, 60))

    buf = io.BytesIO()
    img.save(buf, format="PNG")
    buf.seek(0)
    return buf
