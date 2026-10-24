package cz.animalhouse.testutil

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

object TestClock {

    val FIXED_TODAY: LocalDate =
        LocalDate.of(2026, 10, 19)

    val clock: Clock =
        Clock.fixed(
            FIXED_TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC
        )
}