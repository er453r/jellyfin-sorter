package com.er453r

import mu.KotlinLogging
import org.junit.Test

class JellyfinSorterApp {
    private val logger = KotlinLogging.logger {}

    private val testPaths = arrayOf(
        "./Shrooiking.S02E12.1080p.HEVC.x265-MeGusta/Shrooiking.S02E12.1080p.HEVC.x265-MeGusta.mkv",
        "./The.Walkers.S01.720p.HDTV.X264-DIMENSION/The.Walkers.S01E03.720p.HDTV.X264-DIMENSION.mkv",
        "./Crumbs[Entire Series][Funnyguy263]/Season 8/Crumbs.S08E02.DVDRip.XviD-REWARD.avi",
    )

    private val failPaths = arrayOf(
        "./The.Walkers.S01.720p.HDTV.X264-DIMENSION/The.Walkers.S01E03.sample.720p.HDTV.X264-DIMENSION.mkv",
        "./Crumbs[Entire Series][Funnyguy263]/Season 8 Extras/Crumbs.S08E02.DVDRip.XviD-REWARD.avi",
    )

    @Test
    fun `Sample test`() {
        logger.info { "Sample test!" }

        val seriesRegex = Regex("""^.+/((.+)\.S\d{2}E\d{2}\..*)$""")
        val replacement = "$2/$1"

        testPaths.forEach {
            logger.info { "Testing: $it" }
            logger.info { "Is a series: ${it.replace(seriesRegex, replacement)}" }
        }
    }

    @Test
    fun `Negative test`() {
        logger.info { "Negative test!" }

        val negativeRegex = Regex("""(!derp|sample|extras).+(mp4|mkv|avi)$""", RegexOption.IGNORE_CASE)

        (testPaths + failPaths).forEach {
            logger.info { "Testing: $it" }
            logger.info { "Match: ${negativeRegex.containsMatchIn(it)}" }
        }
    }
}
