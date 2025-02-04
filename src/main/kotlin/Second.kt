package ru.bashcony

// Класс для хранения данных о задаче
data class ProblemData(
    val strategies: List<Int>, // Возможные стратегии (например, количество телевизоров или экскурсоводов)
    val states: List<Int>, // Возможные состояния природы (например, спрос на телевизоры или количество посетителей)
    val payoffs: List<List<Double>>, // Матрица выигрышей/затрат
    val probabilities: List<Double>? = null // Вероятности состояний природы (опционально)
)

// Функция для расчета критерия Лапласа
fun laplaceCriterion(payoffs: List<List<Double>>): Pair<Int, Double> {
    val averages = payoffs.map { row -> row.average() }
    val maxIndex = averages.indexOf(averages.maxOrNull()!!)
    return Pair(maxIndex, averages[maxIndex])
}

// Функция для расчета критерия Вальда
fun waldCriterion(payoffs: List<List<Double>>): Pair<Int, Double> {
    val minValues = payoffs.map { row -> row.minOrNull()!! }
    val maxIndex = minValues.indexOf(minValues.maxOrNull()!!)
    return Pair(maxIndex, minValues[maxIndex])
}

// Функция для расчета критерия Сэвиджа
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
fun hurwiczCriterion(payoffs: List<List<Double>>, alpha: Double): Pair<Int, Double> {
    val hurwiczValues = payoffs.map { row ->
        alpha * row.maxOrNull()!! + (1 - alpha) * row.minOrNull()!!
    }
    val maxIndex = hurwiczValues.indexOf(hurwiczValues.maxOrNull()!!)
    return Pair(maxIndex, hurwiczValues[maxIndex])
}

// Главная функция для решения задачи
fun solveProblem(problem: ProblemData, alpha: Double = 0.5) {
    println("Матрица выигрышей/затрат:")
    problem.payoffs.forEach { println(it) }

    // Критерий Лапласа
    val (laplaceIndex, laplaceValue) = laplaceCriterion(problem.payoffs)
    println("\nКритерий Лапласа:")
    println("Оптимальная стратегия: ${problem.strategies[laplaceIndex]}")
    println("Значение критерия: $laplaceValue")

    // Критерий Вальда
    val (waldIndex, waldValue) = waldCriterion(problem.payoffs)
    println("\nКритерий Вальда:")
    println("Оптимальная стратегия: ${problem.strategies[waldIndex]}")
    println("Значение критерия: $waldValue")

    // Критерий Сэвиджа
    val (savageIndex, savageValue) = savageCriterion(problem.payoffs)
    println("\nКритерий Сэвиджа:")
    println("Оптимальная стратегия: ${problem.strategies[savageIndex]}")
    println("Значение критерия: $savageValue")

    // Критерий Гурвица
    val (hurwiczIndex, hurwiczValue) = hurwiczCriterion(problem.payoffs, alpha)
    println("\nКритерий Гурвица (alpha = $alpha):")
    println("Оптимальная стратегия: ${problem.strategies[hurwiczIndex]}")
    println("Значение критерия: $hurwiczValue")
}

// Пример использования для задачи 1 (производство телевизоров)
fun main() {
    // Данные для задачи 1 (вариант 1)
    val strategies1 = listOf(100, 200, 300, 400) // Возможные стратегии (количество телевизоров)
    val states1 = listOf(100, 200, 300, 400) // Возможные состояния природы (спрос)
    val payoffs1 = listOf(
        listOf(5000.0, 0.0, -5000.0, -10000.0), // Выигрыши для стратегии 100
        listOf(0.0, 10000.0, 5000.0, 0.0),      // Выигрыши для стратегии 200
        listOf(-5000.0, 5000.0, 15000.0, 10000.0), // Выигрыши для стратегии 300
        listOf(-10000.0, 0.0, 10000.0, 20000.0)   // Выигрыши для стратегии 400
    )
    val problem1 = ProblemData(strategies1, states1, payoffs1)
    println("Решение задачи 1 (производство телевизоров):")
    solveProblem(problem1, alpha = 0.5)

    // Данные для задачи 2 (набор экскурсоводов)
    val strategies2 = listOf(1, 2, 3, 4) // Возможные стратегии (количество экскурсоводов)
    val states2 = listOf(50, 100, 150, 200, 250, 300) // Возможные состояния природы (количество посетителей)
    val payoffs2 = listOf(
        listOf(20.0, 20.0, 20.0, 20.0, 20.0, 20.0), // Доходы для 1 экскурсовода
        listOf(40.0, 40.0, 40.0, 40.0, 40.0, 40.0), // Доходы для 2 экскурсоводов
        listOf(60.0, 60.0, 60.0, 60.0, 60.0, 60.0), // Доходы для 3 экскурсоводов
        listOf(80.0, 80.0, 80.0, 80.0, 80.0, 80.0)  // Доходы для 4 экскурсоводов
    )
    val problem2 = ProblemData(strategies2, states2, payoffs2)
    println("\nРешение задачи 2 (набор экскурсоводов):")
    solveProblem(problem2, alpha = 0.5)
}