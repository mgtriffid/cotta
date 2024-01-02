package com.mgtriffid.games.panna.shared.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GeometryTest {

    @ParameterizedTest
    @MethodSource("testOrientationProvider")
    fun testOrientation(
        x1: Int, y1: Int,
        x2: Int, y2: Int,
        x3: Int, y3: Int,
        orientation: Int
    ) {
        assertEquals(orientation, orientation(x1, y1, x2, y2, x3, y3))
    }

    companion object {
        @JvmStatic
        fun testOrientationProvider(): Stream<Arguments> {

            data class Point(val x: Int, val y: Int)
            data class OrientationArguments(val p1: Point, val p2: Point, val p3: Point, val orientation: Int)

            return Stream.of(
                OrientationArguments(Point(0, 0), Point(1, 0), Point(1, 1), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(0, 0), Point(1, 0), Point(1, -1), Orientation.CLOCKWISE),
                OrientationArguments(Point(0, 0), Point(1, 0), Point(2, 0), Orientation.COLLINEAR),
                OrientationArguments(Point(0, 0), Point(1, 0), Point(0, 0), Orientation.COLLINEAR),
                OrientationArguments(Point(0, 0), Point(1, 0), Point(1, 0), Orientation.COLLINEAR),
                OrientationArguments(Point(0, 0), Point(1, 0), Point(0, 1), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(1, 1), Point(3, 3), Point(5, 5), Orientation.COLLINEAR),
                OrientationArguments(Point(1, 1), Point(3, 3), Point(5, 6), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(1, 1), Point(3, 3), Point(5, 4), Orientation.CLOCKWISE),
                OrientationArguments(Point(0, 0), Point(3, 0), Point(5, 1), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(0, 0), Point(3, 0), Point(5, -1), Orientation.CLOCKWISE),
                OrientationArguments(Point(0, 0), Point(3, 0), Point(5, 0), Orientation.COLLINEAR),
                OrientationArguments(Point(5, 5), Point(1, 1), Point(3, 2), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(1, 3), Point(-1, 5), Point(0, 0), Orientation.COUNTERCLOCKWISE),
                OrientationArguments(Point(0, 0), Point(0, 1), Point(0, 2), Orientation.COLLINEAR)
            ).flatMap { (p1, p2, p3, orientation) ->
                Stream.of(
                    OrientationArguments(p1, p2, p3, orientation),
                    OrientationArguments(p2, p3, p1, orientation),
                    OrientationArguments(p3, p1, p2, orientation),
                    OrientationArguments(p1, p3, p2, -orientation),
                    OrientationArguments(p2, p1, p3, -orientation),
                    OrientationArguments(p3, p2, p1, -orientation),
                )
            }.map { (p1, p2, p3, orientation) ->
                Arguments.of(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, orientation)
            }
        }
    }
}
