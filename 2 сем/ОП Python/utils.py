import json
import os
from urllib.parse import quote

from aiogram.types import InlineKeyboardMarkup


def get_env(name: str, default: str = "") -> str:
    return os.getenv(name, default)


def safe_json_dumps(obj) -> str:
    try:
        return json.dumps(obj, ensure_ascii=False)
    except Exception:
        return str(obj)


async def get_bot_start_url(bot) -> str:
    me = await bot.get_me()
    username = getattr(me, "username", None)
    if username:
        return f"https://t.me/{username}?start=quiz"
    return "https://t.me/"


def build_tg_share_url(*, url: str, text: str) -> str:
    # Открывает стандартное окно шаринга в Telegram.
    return f"https://t.me/share/url?url={quote(url)}&text={quote(text)}"
