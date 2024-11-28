package com.example.myprismaapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private final CommandExecutor commandExecutor = new CommandExecutor();

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void modelTest(){
        String activateAndRun =
                "cd ~ && source myenv/bin/activate && " +
                        "python3 /home/alex/AndroidStudioProjects/MyPrismaApp/app/src/main/res/Приложение_Бонд_ipynb_.py";
        commandExecutor.executeCommand(activateAndRun);
    }
}