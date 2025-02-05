package ru.bashcony

import org.apache.commons.math3.linear.*
import org.apache.commons.math3.optim.linear.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun main() {
    // Задаем платежную матрицу

//    val matrix = arrayOf(
//        intArrayOf(0, -1, -2),
//        intArrayOf(1, 0, -1),
//        intArrayOf(2, 1, 0)
//    )

    val matrix = arrayOf(
        intArrayOf(3, 4, 6, 1),
        intArrayOf(2, 8, 4, 3),
        intArrayOf(10, 3, 1, 7),
    )

    // Находим минимаксное и максиминное значения
    val maximin = matrix.map { it.minOrNull() ?: 0 }.maxOrNull() ?: 0
    val minimax = (0 until matrix[0].size).map { j -> matrix.maxOf { it[j] } }.minOrNull() ?: 0

    println("Максиминное значение: $maximin")
    println("Минимаксное значение: $minimax")

    if (maximin == minimax) {
        println("Игра имеет седловую точку со значением $maximin")
    } else {
        println("Игра не имеет седловой точки, ищем решение в смешанных стратегиях")
        println()
        solveMixedStrategies(matrix)
    }
}

fun solveMixedStrategies(matrix: Array<IntArray>) {
    val numRows = matrix.size  // Количество стратегий игрока A
    val numCols = matrix[0].size  // Количество стратегий игрока B

    // Преобразуем матрицу в объект Apache Commons Math для удобства работы с линейной алгеброй
    val coefficients =
        Array2DRowRealMatrix(matrix.map { row -> row.map { it.toDouble() }.toDoubleArray() }.toTypedArray(), false)
    val solver = SimplexSolver()

    // Задаем коэффициенты целевой функции (максимизация минимального выигрыша)
    val objectiveCoefficients = DoubleArray(numRows + 1) { if (it == numRows) 1.0 else 0.0 }
    val objectiveFunction = LinearObjectiveFunction(objectiveCoefficients, 0.0)

    // Задаем ограничения (Ax >= 1), т.е. гарантируем неотрицательный выигрыш
    val constraints = mutableListOf<LinearConstraint>()
    for (j in 0 until numCols) {
        val constraintCoeffs = DoubleArray(numRows + 1) { i -> if (i < numRows) coefficients.getEntry(i, j) else -1.0 }
        constraints.add(LinearConstraint(constraintCoeffs, Relationship.LEQ, 0.0))
    }

    // Добавляем ограничение суммы вероятностей стратегий до 1 (сумма вероятностей = 1)
    val sumCoefficients = DoubleArray(numRows + 1) { if (it < numRows) 1.0 else 0.0 }
    constraints.add(LinearConstraint(sumCoefficients, Relationship.EQ, 1.0))

    // Решаем задачу линейного программирования для поиска смешанной стратегии
    val solution = solver.optimize(
        objectiveFunction,
        LinearConstraintSet(constraints),
        GoalType.MINIMIZE,
        NonNegativeConstraint(true)
    )

    // Извлекаем вероятности выбора стратегий игроком A
    val probabilities = solution.point.take(numRows)

    // Вычисляем средний выигрыш игры (цену игры)
    val gameValue = 1.0 / solution.point.last()
//    println("Значение среднего выигрыша (цена игры): $gameValue")

    // Определение оптимальной стратегии игрока B через двойственную задачу
    val transposedMatrix = coefficients.transpose()
    val bSolver = SimplexSolver()
    val bObjectiveCoefficients = DoubleArray(numCols + 1) { if (it == numCols) 1.0 else 0.0 }
    val bObjectiveFunction = LinearObjectiveFunction(bObjectiveCoefficients, 0.0)
    val bConstraints = mutableListOf<LinearConstraint>()

    for (i in 0 until numRows) {
        val constraintCoeffs = DoubleArray(numCols + 1) { j -> if (j < numCols) transposedMatrix.getEntry(j, i) else -1.0 }
        bConstraints.add(LinearConstraint(constraintCoeffs, Relationship.GEQ, 0.0))
    }

    val bSumCoefficients = DoubleArray(numCols + 1) { if (it < numCols) 1.0 else 0.0 }
    bConstraints.add(LinearConstraint(bSumCoefficients, Relationship.EQ, 1.0))

    val bSolution = bSolver.optimize(
        bObjectiveFunction,
        LinearConstraintSet(bConstraints),
        GoalType.MAXIMIZE,
        NonNegativeConstraint(true)
    )
    val bProbabilities = bSolution.point.take(numCols)
    val bGameValue = 1.0 / bSolution.point.last()

    println("Платежная матрица:")
    matrix.forEach { println(it.joinToString("\t")) }
    println("Средний выигрыш (цена игры) для игрока A: ${gameValue.str()}")
    println("Оптимальная стратегия игрока A: ${probabilities.str()}")
    println("Средний выигрыш (цена игры) для игрока B: ${bGameValue.str()}")
    println("Оптимальная стратегия игрока B: ${bProbabilities.str()}")
}

fun List<Double>.str() = joinToString(separator = ", ", transform = { it.str() })

fun Double.str(): String = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US)).format(this)