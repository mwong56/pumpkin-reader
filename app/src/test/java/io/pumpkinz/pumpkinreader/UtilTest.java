package io.pumpkinz.pumpkinreader;


import org.junit.Test;

import java.util.Calendar;

import io.pumpkinz.pumpkinreader.util.Util;

import static io.pumpkinz.pumpkinreader.util.Util.isDayTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class UtilTest {

    @Test
    public void getDomainName() {
        assertEquals("reddit.com", Util.getDomainName("https://www.reddit.com/r/funny/comments/3cz30y/one_of_the_better_things_ive_seen_at_comic_con/"));
    }

    @Test
    public void testIsDayTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        assertTrue(isDayTime(calendar));

        calendar.set(Calendar.HOUR_OF_DAY, 22);
        assertFalse(isDayTime(calendar));
    }

}
