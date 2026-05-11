from quiz_logic import QuizAnimal, QuizConfig, QuizQuestion, QuizQuestionOption, QuizResult, score_for_question_choice


def test_score_for_choice_accumulates():
    current = {"a": 1}
    choice_scores = {"a": 2, "b": 5}
    out = score_for_question_choice(current, choice_scores)
    assert out["a"] == 3
    assert out["b"] == 5


def test_result_picks_max():
    animals = [
        QuizAnimal(id="x", name="X", description="", fun_fact="", image_url=None),
        QuizAnimal(id="y", name="Y", description="", fun_fact="", image_url=None),
    ]
    questions = [QuizQuestion(id="q", text="?", caption=None, options=[QuizQuestionOption(id="o", text="", scores={})])]
    config = QuizConfig(animals=animals, questions=questions)
    result = QuizResult.from_score(config, {"x": 1, "y": 5})
    assert result.animal.id == "y"
