package liuyao.utils.test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Duishuqi {

    private CountDownLatch latch;
    private int loopSize;
    private int coreSize = Runtime.getRuntime().availableProcessors() * 2;
    private ThreadPoolExecutor pool;

    public Duishuqi(int loopSize) {
        this.loopSize = loopSize * coreSize;
        this.latch = new CountDownLatch(this.loopSize);
        this.pool = new ThreadPoolExecutor(coreSize, coreSize, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r), (r, exee) -> exee.submit(r));
    }

    public Duishuqi(int loopSize, int maxThreads) {
        this.loopSize = loopSize * coreSize;
        this.latch = new CountDownLatch(this.loopSize);
        this.pool = new ThreadPoolExecutor(coreSize, maxThreads, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r), (r, exee) -> exee.submit(r));
    }

    public Duishuqi poolRegistAndRun(Runnable run) {
        for (int i = 0; i < this.loopSize; i++) {
            pool.submit(() -> {
                run.run();
                latch.countDown();
            });
        }
        return this;
    }

    public Duishuqi poolRegistUustrAndRun(Consumer<String> run) {
        final String[] uustrs = new String[this.loopSize];
        for (int i = 0; i < uustrs.length; i++) {
            uustrs[i] = uuString(300, 100);
        }
        for (int i = 0; i < this.loopSize; i++) {
            int finalI = i;
            pool.submit(() -> {
                run.accept(uustrs[finalI]);
                latch.countDown();
            });
        }
        return this;
    }

    public void awaitAndOver() {
        Thread printNotRun = new Thread(() -> {
            for (; ; ) {
                System.out.println("~~~对数器剩余数量: " + latch.getCount());
                if (latch.getCount() == 0) {
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        printNotRun.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();
        printNotRun.interrupt();
        System.out.println("~~~对数器剩余数量: " + latch.getCount());
        System.out.println("~~~对数完成");
    }

    static Random rand = new Random();

    public static String uuString(int maxLen, int charBound) {
        int len = rand.nextInt(maxLen - 1) + 1;
        StringBuilder rt = new StringBuilder();
        for (int i = 0; i < len; i++) {
            rt.append((char) (rand.nextInt(charBound) + 50));
        }

        return rt.toString();
    }

}
