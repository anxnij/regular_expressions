package com.example.a27prac

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a27prac.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sampleText = """
        Журнал дежурного по школе №27.
        Отчёт составлен 14.03.2025 в 09:15 и дополнен 15 марта 2025 года в 21:47:30.
        Сегодня, 02-11-2025, в 08:00 у кабинета информатики произошло следующее:
        ученик Иванов учился, доучивался, переучивал материал по регулярным выражениям.
        Учитель сказала, что контрольная работа перенесена на 18.11.2025.
        В 12:30 прошла встреча кружка робототехники.
        В 17:05 директор школы подписал приказ.
        Также зафиксированы разговоры по телефону с родителями:
        - Алло... мама сейчас не может говорить, она в душе...
        - Мама ушла в магазин, её не будет до 18:00...
        - А кто это? Папы нет дома... я одна.
        - Ммм... мама спит, перезвоните потом...
        - Да-да, здравствуйте, я секретарь, соединяю на директора школы.
        Запланированные мероприятия:
        16.03.2025 — олимпиада по математике в 10:00.
        17.03.2025 — экскурсия в музей к 11:45.
        28-03-2025 — общешкольное собрание в актовом зале.
        Напоминание: не забудьте заполнить форму обратной связи до 20.03.2025.
        Также, напоминаем про безопасность:
        Никому не сообщайте пароль, пин-код, код подтверждения, данные карты.
        Пример длинных слов: электрификация, гиперответственность, коммуникация, конфиденциальность, воображение.
        Конец отчёта.
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSampleText.text = sampleText

        // Проверка наличия указанного слова (строго как отдельного)
        binding.btnCheckWordExact.setOnClickListener {
            val word = binding.etWordExact.text.toString().trim()
            val result = checkExactWord(sampleText, word) // ← реализация в функции ниже
            binding.tvResultExact.text = if (result) {
                "Слово «$word» есть (точное совпадение)"
            } else {
                "Слова «$word» НЕТ как отдельного слова"
            }
        }

        // Проверка слова с учётом различных окончаний
        binding.btnCheckWordWithEnding.setOnClickListener {
            val stem = binding.etWordWithEnding.text.toString().trim()
            val matches = findWordWithEndings(sampleText, stem) // ← реализация в функции ниже
            binding.tvResultEnding.text =
                if (matches.isNotEmpty()) {
                    "Нашли формы: ${matches.joinToString()}"
                } else {
                    "Не нашли форм слова с основой «$stem»"
                }
        }

        // Найти в тексте все слова с одинаковым «корнем»
        binding.btnFindRoot.setOnClickListener {
            val root = binding.etRoot.text.toString().trim()
            val list = findSameRootWords(sampleText, root) // ← реализация в функции ниже
            binding.tvResultRoot.text = if (list.isNotEmpty()) {
                "Слова с корнем «$root»: ${list.joinToString()}"
            } else {
                "Слова с корнем «$root» не найдены"
            }
        }

        // Посчитать количество слов, длиннее 5 символов
        binding.btnCountLongWords.setOnClickListener {
            val count = countLongWords(sampleText, 5) // ← реализация в функции ниже
            binding.tvResultLongWords.text = "Длинных слов (>5 букв): $count"
        }

        // Найти все даты
        binding.btnFindDates.setOnClickListener {
            val dates = findDates(sampleText) // ← реализация в функции ниже
            binding.tvResultDates.text = if (dates.isNotEmpty()) {
                "Даты: ${dates.joinToString()}"
            } else {
                "Дат не найдено"
            }
        }

        // Найти всё время (HH:mm[:ss])
        binding.btnFindTimes.setOnClickListener {
            val times = findTimes(sampleText) // ← реализация в функции ниже
            binding.tvResultTimes.text = if (times.isNotEmpty()) {
                "Время: ${times.joinToString()}"
            } else {
                "Время не найдено"
            }
        }

        // Разделить текст по определённому слову
        binding.btnSplitText.setOnClickListener {
            val word = binding.etSplitWord.text.toString().trim()
            val parts = splitByWord(sampleText, word) // ← реализация в функции ниже
            binding.tvResultSplit.text = if (parts.isNotEmpty()) {
                "Текст разбит на ${parts.size} частей:\n${parts.joinToString(separator = "\n---\n")}"
            } else {
                "Не получилось разбить (пустое слово?)"
            }
        }

        // Определить по тексту ответа на телефонный звонок, что ответил ребёнок
        binding.btnIsChild.setOnClickListener {
            val isChild = containsChildResponder(sampleText) // ← реализация (эвристики) ниже
            binding.tvResultChild.text = if (isChild) {
                "Похоже, отвечает ребёнок"
            } else {
                "Скорее взрослый"
            }
        }
    }

    // Пробегаем по строкам и ищем «детские» ответы (эвристика)
    private fun containsChildResponder(text: String): Boolean {
        // Берём все непустые строки; можно фильтровать только «реплики» с дефисом, но берём шире
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (line in lines) {
            if (isChildAnswer(line)) return true // если хоть одна реплика похожа — считаем «ребёнок»
        }
        return false
    }

    // Детектор «детского ответа» с набором паттернов и контекстов
    private fun isChildAnswer(answer: String): Boolean {
        if (answer.isBlank()) return false

        // Нормализация: только буквы/цифры/пробелы, нижний регистр
        val t = answer.lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Сильные паттерны — достаточно одного совпадения
        val strongPatterns = listOf(
            "мам[аы]\\s+нет\\s+дома",
            "пап[аы]\\s+нет\\s+дома",
            "родител(ей|и)\\s+нет",
            "я\\s+один(а)?(\\s+дома)?",
            "мама\\s+ушла(\\s+в\\s+магазин)?",
            "мама\\s+в\\s+душе",
            "мама\\s+спит",
            "мама\\s+занята",
            "мама\\s+не\\s+может\\s+говорить",
            "перезвон(ите)?\\s+(позже|потом)",
            "(а\\s+)?кто\\s+это",
            "сейчас\\s+позову\\s+мам(у|а)|позов(у|ать)\\s+мам(у|а)|могу\\s+позвать\\s+(маму|папу)"
        )
        if (strongPatterns.any { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(t) }) return true

        // Комбинации «родитель + состояние»
        val mentionsParent = Regex("\\b(мама|мамы|папа|папы|родители)\\b").containsMatchIn(t)
        val childContext  = Regex("\\b(нет|ушл[аи]|спит|занят[аи]|в\\s+душе|не\\s+может)\\b").containsMatchIn(t)
        if (mentionsParent && childContext) return true

        // Мягкие маркеры + упоминание родителя
        val softMarkers = listOf("не знаю", "я не знаю", "секундочку", "щас", "сейчас позову")
        val softHit = softMarkers.any { t.contains(it) }
        return mentionsParent && softHit
    }

    // Реализация «точного слова» (границы слова \b)
    private fun checkExactWord(text: String, word: String): Boolean {
        if (word.isEmpty()) return false
        val pattern = Regex("\\b" + Regex.escape(word) + "\\b", RegexOption.IGNORE_CASE)
        return pattern.containsMatchIn(text)
    }

    // Реализация «слово с окончаниями» — основа + любые буквы
    private fun findWordWithEndings(text: String, stem: String): List<String> {
        if (stem.isEmpty()) return emptyList()
        val pattern = Regex(
            "\\b" + Regex.escape(stem) + "[\\p{L}]*\\b",
            setOf(RegexOption.IGNORE_CASE)
        )
        return pattern.findAll(text).map { it.value }.distinct().toList()
    }

    // Поиск слов с одинаковым «корнем» (простая эвристика — тот же паттерн, нормализуем к lowercase)
    private fun findSameRootWords(text: String, root: String): List<String> {
        if (root.isEmpty()) return emptyList()
        val pattern = Regex(
            "\\b" + Regex.escape(root) + "[\\p{L}]*\\b",
            setOf(RegexOption.IGNORE_CASE)
        )
        return pattern.findAll(text).map { it.value }
            .map { it.lowercase(Locale.getDefault()) }
            .distinct()
            .toList()
    }

    // Подсчёт «длинных слов»: длина > minLen (здесь >5)
    private fun countLongWords(text: String, minLen: Int): Int {
        // {${minLen + 1},} — строго больше minLen
        val regex = Regex("\\b\\p{L}{${minLen + 1},}\\b", RegexOption.IGNORE_CASE)
        return regex.findAll(text).count()
    }

    // Поиск дат: числовые (dd.mm.yyyy / dd-mm-yyyy / dd/mm/yy) и текстовые («15 марта 2025»)
    private fun findDates(text: String): List<String> {
        val numericDate = "\\b(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4})\\b"
        val russianMonths = "(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)"
        val textDate = "\\b(\\d{1,2}\\s+" + russianMonths + "\\s+\\d{4})\\b"
        val combined = Regex("$numericDate|$textDate", RegexOption.IGNORE_CASE)
        return combined.findAll(text).map { it.value }.toList()
    }

    // Поиск времени: HH:mm или HH:mm:ss (24-часовой формат)
    private fun findTimes(text: String): List<String> {
        val timeRegex = Regex(
            "\\b([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?\\b"
        )
        return timeRegex.findAll(text).map { it.value }.toList()
    }

    // Разделение текста по слову (регистронезависимо)
    private fun splitByWord(text: String, word: String): List<String> {
        if (word.isEmpty()) return emptyList()
        val regex = Regex("(?i)" + Regex.escape(word))
        return text.split(regex)
    }
}
