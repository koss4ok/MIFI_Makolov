from __future__ import annotations

import json
import os
from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class QuizAnimal:
    id: str
    name: str
    description: str
    fun_fact: str
    image_url: str | None


@dataclass(frozen=True)
class QuizQuestionOption:
    id: str
    text: str
    scores: dict[str, int]


@dataclass(frozen=True)
class QuizQuestion:
    id: str
    text: str
    caption: str | None
    options: list[QuizQuestionOption]


@dataclass(frozen=True)
class QuizConfig:
    animals: list[QuizAnimal]
    questions: list[QuizQuestion]
    animals_by_id: dict[str, QuizAnimal]

    def __init__(self, animals: list[QuizAnimal], questions: list[QuizQuestion]):
        object.__setattr__(self, "animals", animals)
        object.__setattr__(self, "questions", questions)
        object.__setattr__(self, "animals_by_id", {a.id: a for a in animals})


@dataclass(frozen=True)
class QuizResult:
    animal: QuizAnimal
    score: dict[str, int]

    @staticmethod
    def from_score(config: QuizConfig, score: dict[str, int]) -> "QuizResult":
        best_id: str | None = None
        best_val: int | None = None
        for animal_id in config.animals_by_id.keys():
            v = int(score.get(animal_id, 0))
            if best_val is None or v > best_val or (v == best_val and (best_id is None or animal_id < best_id)):
                best_val = v
                best_id = animal_id
        assert best_id is not None
        return QuizResult(animal=config.animals_by_id[best_id], score=score)

    def to_dict(self) -> dict[str, Any]:
        return {"animal_id": self.animal.id, "animal_name": self.animal.name, "score": self.score}


def score_for_question_choice(current: dict[str, int], choice_scores: dict[str, int]) -> dict[str, int]:
    out = dict(current)
    for animal_id, pts in choice_scores.items():
        out[animal_id] = int(out.get(animal_id, 0)) + int(pts)
    return out


def load_quiz_config() -> QuizConfig:
    config_path = os.path.join(os.path.dirname(__file__), "quiz_config.json")
    with open(config_path, "r", encoding="utf-8") as f:
        raw = json.load(f)

    animals = [
        QuizAnimal(
            id=a["id"],
            name=a["name"],
            description=a["description"],
            fun_fact=a["fun_fact"],
            image_url=a.get("image_url"),
        )
        for a in raw["animals"]
    ]

    questions = [
        QuizQuestion(
            id=q["id"],
            text=q["text"],
            caption=q.get("caption"),
            options=[
                QuizQuestionOption(
                    id=o["id"],
                    text=o["text"],
                    scores=o["scores"],
                )
                for o in q["options"]
            ],
        )
        for q in raw["questions"]
    ]

    return QuizConfig(animals=animals, questions=questions)
