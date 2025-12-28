package com.meng.bot.qq.modules;

import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class StepCommandProcessor<T> extends BaseModule implements IGroupMessageEvent {

    protected Map<Long, StepRunnable<T>> steps = new HashMap<>();

    public StepCommandProcessor(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        long qq = event.getSender().getId();
        StepRunnable<T> stepRunnable = steps.get(qq);
        if (stepRunnable == null) {
            return false;
        }
        if (!stepRunnable.prepare.apply(event, stepRunnable)) {
            StepRunnable<T> remove = steps.remove(stepRunnable);
            if (remove != null) {
                remove.cancel(event);
            }
            return true;
        }
        stepRunnable.transactCommand.accept(event, stepRunnable);
        return true;
    }

    public void addOnAction(long senderId, StepRunnable<T> runnable) {
        steps.put(senderId, runnable);
    }

    public void cancel(GroupMessageEvent event) {
        steps.remove(event.getSender().getId()).cancel(event);
    }

    public static class StepRunnable<T> {
        private int step = 0;
        private int loopPoint = -1;
        public T extra;
        public BiFunction<GroupMessageEvent, StepRunnable<T>, Boolean> prepare = new BiFunction<GroupMessageEvent, StepRunnable<T>, Boolean>() {

            @Override
            public Boolean apply(GroupMessageEvent event, StepRunnable<T> runnable) {
                return step < actions.size();
            }
        };
        public BiConsumer<GroupMessageEvent, StepRunnable<T>> transactCommand = new BiConsumer<GroupMessageEvent, StepRunnable<T>>() {

            @Override
            public void accept(GroupMessageEvent event, StepRunnable<T> runnable) {
                runnable.run(event);
            }
        };

        public ArrayList<BiConsumer<GroupMessageEvent, StepRunnable<T>>> actions = new ArrayList<>();

        public Consumer<GroupMessageEvent> onStop = new Consumer<GroupMessageEvent>() {

            @Override
            public void accept(GroupMessageEvent event) {
                event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("任务已取消"));
            }
        };

        public void set(BiFunction<GroupMessageEvent, StepRunnable<T>, Boolean> prepare) {
            this.prepare = prepare;
        }

        public void set(BiConsumer<GroupMessageEvent, StepRunnable<T>> transactCommand) {
            this.transactCommand = transactCommand;
        }

        public void set(Consumer<GroupMessageEvent> runnable) {
            onStop = runnable;
        }

        public void setLoopPoint() {
            if (loopPoint != -1) {
                return;
            }
            loopPoint = actions.size();
        }

        public void gotoLoopPoint() {
            step = loopPoint;
        }

        public void addActions(BiConsumer<GroupMessageEvent, StepRunnable<T>>... runnables) {
            Collections.addAll(actions, runnables);
        }

        public void run(GroupMessageEvent event) {
            actions.get(step++).accept(event, this);
        }

        private void cancel(GroupMessageEvent event) {
            step = actions.size();
            if (onStop != null) {
                onStop.accept(event);
            }
        }
    }
}
