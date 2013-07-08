package org.deephacks.westty.job;

import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@Schedule("*/2 * * * * ?")
@RunWith(WesttyJUnit4Runner.class)
public class JobTest implements Job {
    private static Integer count = 0;
    private static final LinkedList<Long> timeouts = new LinkedList<>();

    /**
     * Test that timeouts occur on a schedule of 2 seconds and that
     * we can store data in the job map.
     */
    @Test
    public void test_job() throws InterruptedException {
        Thread.sleep(10000);
        synchronized (timeouts) {
            assertThat(timeouts.size(), is(count));
            assertTrue(timeouts.size() > 2);
        }
    }

    @Override
    public void execute(JobData map) {
        synchronized (timeouts){
            String str = map.get("count");

            if(str == null) {
                map.put("count", 1 + "");
            } else {
                Integer i = Integer.parseInt(str);
                i++;
                count = i;
                map.put("count", i.toString());
            }
            timeouts.addFirst(System.currentTimeMillis());
        }
    }
}
