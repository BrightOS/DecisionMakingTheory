import de.vandermeer.asciitable.AsciiTable
import kotlin.math.*

// Класс для хранения данных о задаче
data class ProblemData(
    // Возможные стратегии (например, количество телевизоров или экскурсоводов)
    val strategies: List<Int>,
    // Возможные состояния природы (например, спрос на телевизоры или количество посетителей)
    val states: List<Int>,
    // Вероятности состояний природы (опционально)
    val probabilities: List<Double>? = null,
    // Матрица выигрышей/затрат (опционально)
    val payoffs: List<List<Double>>? = null,
)

// Функция для расчета критерия Лапласа
fun laplaceCriterion(payoffs: List<List<Double>>): Pair<Int, Double> {
    val averages = payoffs.map { row -> row.average() }
    val maxIndex = averages.indexOf(averages.maxOrNull()!!)
    return Pair(maxIndex, averages[maxIndex])
}

// Функция для расчета критерия Вальда
// Природа даёт нам максимальные потери
fun waldCriterion(payoffs: List<List<Double>>): Pair<Int, Double> {
    val minValues = payoffs.map { row -> row.minOrNull()!! }
    val maxIndex = minValues.indexOf(minValues.maxOrNull()!!)
    return Pair(maxIndex, minValues[maxIndex])
}

// Функция для расчета критерия Сэвиджа
// Минимизация матрицы рисков
fun savageCriterion(payoffs: List<List<Double>>): Pair<Int, Double> {
    val regretMatrix = mutableListOf<List<Double>>()
    for (row in payoffs) {
        val regretRow = row.indices.map { j ->
            payoffs.maxOf { it[j] } - row[j]
        }
        regretMatrix.add(regretRow)
    }
    val maxRegrets = regretMatrix.map { it.maxOrNull()!! }
    val minIndex = maxRegrets.indexOf(maxRegrets.minOrNull()!!)
    return Pair(minIndex, maxRegrets[minIndex])
}

// Функция для расчета критерия Гурвица
// Использует оба подхода с использованием коэффициента доверия
fun hurwiczCriterion(payoffs: List<List<Double>>, alpha: Double): Pair<Int, Double> {
    val hurwiczValues = payoffs.map { row ->
        alpha * row.maxOrNull()!! + (1 - alpha) * row.minOrNull()!!
    }
    val maxIndex = hurwiczValues.indexOf(hurwiczValues.maxOrNull()!!)
    return Pair(maxIndex, hurwiczValues[maxIndex])
}

// Функция для расчета матрицы выигрышей для задачи 1 (производство телевизоров)
fun calculatePayoffsTask1(strategies: List<Int>, states: List<Int>, price: Double, cost: Double): List<List<Double>> {
    return strategies.map { A ->
        states.map { S ->
            val sales = min(A, S) // Фактический объем продаж
            val surplus = max(0, A - S) // Излишек продукции
            sales * (price - cost) - surplus * cost
        }
    }
}

// Функция для расчета матрицы выигрышей для задачи 2 (набор экскурсоводов)
fun calculatePayoffsTask2(
    strategies: List<Int>,
    states: List<Int>,
    exhibitionCost: Double,
    guideSalary: Double,
    ticketPrice: Double
): List<List<Double>> {
    return strategies.map { E ->
        states.map { S ->
            val maxVisitors = E * 100 // Максимальное количество обслуженных посетителей
            val servedVisitors = min(maxVisitors, S) // Фактическое количество обслуженных посетителей
            servedVisitors * ticketPrice - (exhibitionCost + E * guideSalary)
        }
    }
}

// Главная функция для решения задачи
fun solveProblem(problem: ProblemData, alpha: Double = 0.5) {
    val payoffs = problem.payoffs ?: error("Матрица выигрышей не задана")
    println("Матрица выигрышей/затрат:")
    AsciiTable().apply {
        addRule()
        problem.payoffs.forEach {
            addRow(*it.toTypedArray())
            addRule()
        }
    }.render().let {
        println(it)
    }

    // Критерий Лапласа
    val (laplaceIndex, laplaceValue) = laplaceCriterion(payoffs)
    println("\nКритерий Лапласа:")
    println("Оптимальная стратегия: ${problem.strategies[laplaceIndex]}")
    println("Значение критерия: $laplaceValue")

    // Критерий Вальда
    val (waldIndex, waldValue) = waldCriterion(payoffs)
    println("\nКритерий Вальда:")
    println("Оптимальная стратегия: ${problem.strategies[waldIndex]}")
    println("Значение критерия: $waldValue")

    // Критерий Сэвиджа
    val (savageIndex, savageValue) = savageCriterion(payoffs)
    println("\nКритерий Сэвиджа:")
    println("Оптимальная стратегия: ${problem.strategies[savageIndex]}")
    println("Значение критерия: $savageValue")

    // Критерий Гурвица
    val (hurwiczIndex, hurwiczValue) = hurwiczCriterion(payoffs, alpha)
    println("\nКритерий Гурвица (alpha = $alpha):")
    println("Оптимальная стратегия: ${problem.strategies[hurwiczIndex]}")
    println("Значение критерия: $hurwiczValue")
}

// Пример использования для задачи 1 (производство телевизоров)
fun main() {
    // Данные для задачи 1 (вариант 1)
    val strategies1 = listOf(100, 200, 300, 400) // Возможные стратегии (количество телевизоров)
    val states1 = listOf(100, 200, 300, 400) // Возможные состояния природы (спрос)
    val price1 = 100.0 // Отпускная цена одного телевизора
    val cost1 = 50.0 // Полные затраты на производство одного телевизора

    // Расчет матрицы выигрышей
    val payoffs1 = calculatePayoffsTask1(strategies1, states1, price1, cost1)
    val problem1 = ProblemData(strategies1, states1, payoffs = payoffs1)
    println("Решение задачи 1 (производство телевизоров):")
    solveProblem(problem1, alpha = 0.5)

    // Данные для задачи 2 (набор экскурсоводов)
    val strategies2 = listOf(1, 2, 3, 4) // Возможные стратегии (количество экскурсоводов)
    val states2 = listOf(50, 100, 150, 200, 250, 300) // Возможные состояния природы (количество посетителей)
    val exhibitionCost2 = 80.0 // Затраты на содержание выставки в день
    val guideSalary2 = 40.0 // Заработная плата одного экскурсовода в день
    val ticketPrice2 = 2.0 // Цена билета

    // Расчет матрицы выигрышей
    val payoffs2 = calculatePayoffsTask2(strategies2, states2, exhibitionCost2, guideSalary2, ticketPrice2)
    val problem2 = ProblemData(strategies2, states2, payoffs = payoffs2)
    println("\nРешение задачи 2 (набор экскурсоводов):")
    solveProblem(problem2, alpha = 0.5)
}