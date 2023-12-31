package org.spiderflow.core.executor.function;

import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * Created on 2019-12-06
 *
 * @author Octopus
 */
@Component
@Comment("threadCommon Methods")
public class ThreadFunctionExecutor implements FunctionExecutor {
    @Override
    public String getFunctionPrefix() {
        return "thread";
    }

    @Comment("Threads")
    @Example("${thread.sleep(1000L)}")
    public static void sleep(Long sleepTime){
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
