package com.example.game

import com.example.data.model.ArrowMazeItem
import com.example.data.model.Direction
import com.example.data.model.GridPoint
import com.example.data.model.LevelConfig
import java.util.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object LevelGenerator {

    /**
     * Generates a 100% solvable winding arrow maze level out of 20,000 levels.
     * Guaranteed:
     * 1. Arrows are small, delicate, and compact (high grid density).
     * 2. EVERY SINGLE ARROW has a guaranteed exit path out of the box.
     * 3. No two arrows face head-to-head directly.
     */
    fun generateLevel(levelNumber: Int): LevelConfig {
        val seed = (levelNumber.toLong() * 1103515245L + 12345L) and 0x7fffffffL
        val random = Random(seed)

        // Denser grid for smaller, more delicate arrows
        val (rows, cols, targetArrowCount) = when {
            levelNumber <= 5 -> Triple(14, 10, 12 + levelNumber * 2)
            levelNumber <= 20 -> Triple(18, 14, 22 + (levelNumber - 5))
            levelNumber <= 100 -> Triple(22, 16, 32 + (levelNumber - 20) / 3)
            levelNumber <= 500 -> Triple(26, 18, 42 + (levelNumber - 100) / 15)
            levelNumber <= 2000 -> Triple(28, 20, 52 + (levelNumber - 500) / 80)
            else -> Triple(30, 22, 60 + min(20, (levelNumber - 2000) / 900))
        }

        val difficulty = when {
            levelNumber <= 20 -> "সহজ স্তর"
            levelNumber <= 100 -> "মধ্যম স্তর"
            levelNumber <= 1000 -> "চ্যালেঞ্জিং স্তর"
            levelNumber <= 5000 -> "অভিজ্ঞ স্তর"
            else -> "মাস্টার স্তর"
        }

        val occupiedCells = mutableMapOf<Pair<Int, Int>, Int>()
        val arrowHeadDirs = mutableMapOf<Pair<Int, Int>, Direction>()
        val rawArrows = mutableListOf<ArrowMazeItem>()

        var arrowIdCounter = 1
        var attempts = 0
        val maxAttempts = targetArrowCount * 6

        while (rawArrows.size < targetArrowCount && attempts < maxAttempts) {
            attempts++

            val headR = random.nextInt(rows)
            val headC = random.nextInt(cols)

            if (occupiedCells.containsKey(Pair(headR, headC))) continue

            // Candidate directions from head
            val validDirs = Direction.values().filter { dir ->
                isExitClearFromHead(headR, headC, dir, occupiedCells, arrowHeadDirs, rows, cols)
            }

            if (validDirs.isEmpty()) continue
            val chosenDir = validDirs[random.nextInt(validDirs.size)]

            // Generate a compact bending arrow body
            val arrowPath = generateCompactBendingPath(
                headR = headR,
                headC = headC,
                headDir = chosenDir,
                occupiedCells = occupiedCells,
                rows = rows,
                cols = cols,
                random = random
            )

            if (arrowPath != null && arrowPath.size >= 2) {
                val arrowId = arrowIdCounter++
                for (pt in arrowPath) {
                    occupiedCells[Pair(pt.row, pt.col)] = arrowId
                }
                arrowHeadDirs[Pair(headR, headC)] = chosenDir

                val vertices = simplifyToVertices(arrowPath)
                rawArrows.add(
                    ArrowMazeItem(
                        id = arrowId,
                        points = vertices,
                        headDirection = chosenDir
                    )
                )
            }
        }

        // Guarantee 100% Solvability:
        // Filter and prune any arrow that is stuck in a mutual lock so that
        // EVERY SINGLE ARROW in the returned list is mathematically guaranteed to have an exit path!
        val guaranteedSolvableArrows = pruneUnsolvableArrows(rawArrows, rows, cols)

        // If after pruning, arrows are fewer than 6, generate guaranteed perimeter exit arrows
        val finalArrows = if (guaranteedSolvableArrows.size < 6) {
            val augmented = guaranteedSolvableArrows.toMutableList()
            val perimeterExits = listOf(
                Pair(0, cols / 2) to Direction.UP,
                Pair(rows - 1, cols / 2) to Direction.DOWN,
                Pair(rows / 2, 0) to Direction.LEFT,
                Pair(rows / 2, cols - 1) to Direction.RIGHT,
                Pair(0, cols / 4) to Direction.UP,
                Pair(rows - 1, (cols * 3) / 4) to Direction.DOWN
            )
            val activeOccupancy = mutableSetOf<Pair<Int, Int>>()
            for (arr in augmented) {
                for (pt in interpolatePath(arr.points)) {
                    activeOccupancy.add(Pair(pt.row, pt.col))
                }
            }

            for ((pos, dir) in perimeterExits) {
                val (r, c) = pos
                val tailR = (r - dir.deltaRow * 2).coerceIn(0, rows - 1)
                val tailC = (c - dir.deltaCol * 2).coerceIn(0, cols - 1)
                if (!activeOccupancy.contains(Pair(r, c)) && !activeOccupancy.contains(Pair(tailR, tailC))) {
                    augmented.add(
                        ArrowMazeItem(
                            id = arrowIdCounter++,
                            points = listOf(GridPoint(tailR, tailC), GridPoint(r, c)),
                            headDirection = dir
                        )
                    )
                    activeOccupancy.add(Pair(r, c))
                    activeOccupancy.add(Pair(tailR, tailC))
                }
            }
            augmented
        } else {
            guaranteedSolvableArrows
        }

        return LevelConfig(
            levelNumber = levelNumber,
            gridRows = rows,
            gridCols = cols,
            arrows = finalArrows,
            difficultyBn = difficulty
        )
    }

    /**
     * Checks if moving in headDir from (r, c) reaches board edge without hitting another arrow,
     * AND strictly guarantees that no other arrow head is pointing in the opposite direction.
     */
    private fun isExitClearFromHead(
        r: Int,
        c: Int,
        dir: Direction,
        occupied: Map<Pair<Int, Int>, Int>,
        arrowHeadDirs: Map<Pair<Int, Int>, Direction>,
        rows: Int,
        cols: Int
    ): Boolean {
        var currR = r + dir.deltaRow
        var currC = c + dir.deltaCol

        while (currR in 0 until rows && currC in 0 until cols) {
            val coord = Pair(currR, currC)
            if (occupied.containsKey(coord)) {
                val otherDir = arrowHeadDirs[coord]
                if (otherDir != null && otherDir.deltaRow == -dir.deltaRow && otherDir.deltaCol == -dir.deltaCol) {
                    return false // Strictly forbid head-to-head collision!
                }
            }
            currR += dir.deltaRow
            currC += dir.deltaCol
        }

        // Also check behind to prevent any opposite head pointing right at this head
        var oppR = r - dir.deltaRow
        var oppC = c - dir.deltaCol
        while (oppR in 0 until rows && oppC in 0 until cols) {
            val oppHeadDir = arrowHeadDirs[Pair(oppR, oppC)]
            if (oppHeadDir == dir) {
                return false
            }
            oppR -= dir.deltaRow
            oppC -= dir.deltaCol
        }

        return true
    }

    /**
     * Generates a compact winding path with shorter segments for smaller, refined arrows.
     */
    private fun generateCompactBendingPath(
        headR: Int,
        headC: Int,
        headDir: Direction,
        occupiedCells: Map<Pair<Int, Int>, Int>,
        rows: Int,
        cols: Int,
        random: Random
    ): List<GridPoint>? {
        val path = mutableListOf<GridPoint>()
        path.add(GridPoint(headR, headC))

        val backDirR = -headDir.deltaRow
        val backDirC = -headDir.deltaCol

        // Compact first segment: 1 to 3 cells
        val firstSegLength = random.nextInt(3) + 1
        var currR = headR
        var currC = headC

        for (i in 0 until firstSegLength) {
            val nextR = currR + backDirR
            val nextC = currC + backDirC
            if (nextR !in 0 until rows || nextC !in 0 until cols) break
            if (occupiedCells.containsKey(Pair(nextR, nextC))) break
            currR = nextR
            currC = nextC
            path.add(GridPoint(currR, currC))
        }

        if (path.size < 2) return null

        // 70% chance to add a 2nd compact bend
        if (random.nextFloat() < 0.7f) {
            val turnDirs = if (backDirR != 0) {
                listOf(Pair(0, 1), Pair(0, -1))
            } else {
                listOf(Pair(1, 0), Pair(-1, 0))
            }
            val (dR, dC) = turnDirs[random.nextInt(turnDirs.size)]
            val secondSegLength = random.nextInt(3) + 1

            for (i in 0 until secondSegLength) {
                val nextR = currR + dR
                val nextC = currC + dC
                if (nextR !in 0 until rows || nextC !in 0 until cols) break
                if (occupiedCells.containsKey(Pair(nextR, nextC))) break
                currR = nextR
                currC = nextC
                path.add(GridPoint(currR, currC))
            }

            // 40% chance to add a 3rd bend (compact U-turn)
            if (random.nextFloat() < 0.4f) {
                val thirdDirR = -backDirR
                val thirdDirC = -backDirC
                val thirdSegLength = random.nextInt(2) + 1

                for (i in 0 until thirdSegLength) {
                    val nextR = currR + thirdDirR
                    val nextC = currC + thirdDirC
                    if (nextR !in 0 until rows || nextC !in 0 until cols) break
                    if (occupiedCells.containsKey(Pair(nextR, nextC))) break
                    currR = nextR
                    currC = nextC
                    path.add(GridPoint(currR, currC))
                }
            }
        }

        return path.reversed()
    }

    private fun simplifyToVertices(fullPath: List<GridPoint>): List<GridPoint> {
        if (fullPath.size <= 2) return fullPath

        val vertices = mutableListOf<GridPoint>()
        vertices.add(fullPath.first())

        for (i in 1 until fullPath.size - 1) {
            val prev = fullPath[i - 1]
            val curr = fullPath[i]
            val next = fullPath[i + 1]

            val d1R = curr.row - prev.row
            val d1C = curr.col - prev.col
            val d2R = next.row - curr.row
            val d2C = next.col - curr.col

            if (d1R != d2R || d1C != d2C) {
                vertices.add(curr)
            }
        }

        vertices.add(fullPath.last())
        return vertices
    }

    /**
     * Checks if an arrow's exit path is currently clear of any other active arrows.
     */
    fun isArrowPathClear(
        arrow: ArrowMazeItem,
        allArrows: List<ArrowMazeItem>,
        rows: Int,
        cols: Int
    ): Boolean {
        val head = arrow.points.last()
        val dir = arrow.headDirection

        val activePoints = mutableSetOf<Pair<Int, Int>>()
        for (other in allArrows) {
            if (other.id != arrow.id && !other.isCleared) {
                val interpolated = interpolatePath(other.points)
                for (pt in interpolated) {
                    activePoints.add(Pair(pt.row, pt.col))
                }
            }
        }

        var currR = head.row + dir.deltaRow
        var currC = head.col + dir.deltaCol

        while (currR in 0 until rows && currC in 0 until cols) {
            if (activePoints.contains(Pair(currR, currC))) {
                return false // Blocked by an active arrow!
            }
            currR += dir.deltaRow
            currC += dir.deltaCol
        }
        return true // Completely clear exit path!
    }

    /**
     * Simulates forward gameplay and prunes any arrow that cannot exit,
     * guaranteeing that 100% of the remaining arrows have a valid escape route out of the box!
     */
    fun pruneUnsolvableArrows(
        arrows: List<ArrowMazeItem>,
        rows: Int,
        cols: Int
    ): List<ArrowMazeItem> {
        val solvableSet = mutableSetOf<Int>()
        val currentRemaining = arrows.toMutableList()

        var progress = true
        while (progress && currentRemaining.isNotEmpty()) {
            progress = false
            val it = currentRemaining.iterator()
            while (it.hasNext()) {
                val arrow = it.next()
                if (isArrowPathClear(arrow, currentRemaining, rows, cols)) {
                    solvableSet.add(arrow.id)
                    it.remove()
                    progress = true
                }
            }
        }

        // Return only arrows that were solved in the simulation
        return arrows.filter { solvableSet.contains(it.id) }
    }

    /**
     * Verifies if all arrows in the level can be cleared.
     */
    fun canSolveAll(arrows: List<ArrowMazeItem>, rows: Int, cols: Int): Boolean {
        val remaining = arrows.toMutableList()
        var changed = true
        while (changed && remaining.isNotEmpty()) {
            changed = false
            val it = remaining.iterator()
            while (it.hasNext()) {
                val arrow = it.next()
                if (isArrowPathClear(arrow, remaining, rows, cols)) {
                    it.remove()
                    changed = true
                }
            }
        }
        return remaining.isEmpty()
    }

    fun interpolatePath(vertices: List<GridPoint>): List<GridPoint> {
        if (vertices.size <= 1) return vertices
        val result = mutableListOf<GridPoint>()

        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i]
            val p2 = vertices[i + 1]

            val dR = (p2.row - p1.row).coerceIn(-1, 1)
            val dC = (p2.col - p1.col).coerceIn(-1, 1)

            var r = p1.row
            var c = p1.col

            while (r != p2.row || c != p2.col) {
                result.add(GridPoint(r, c))
                r += dR
                c += dC
            }
        }
        result.add(vertices.last())
        return result
    }
}
