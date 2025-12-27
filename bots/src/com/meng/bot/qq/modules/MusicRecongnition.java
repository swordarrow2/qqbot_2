package com.meng.bot.qq.modules;

import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.command.SecondaryCommand;
import com.meng.bot.qq.commonModules.UserInfoManager;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.CmdExecuter;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.normal.FfmpegCmdBuilder;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import com.meng.tools.normal.TextLexer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jaudiotagger.audio.AudioFileIO;

public class MusicRecongnition extends BaseModule implements IGroupMessageEvent {

    public static String musicFolder = "C://thbgm/";
    private final ArrayList<String> musicNames = new ArrayList<>();
    private final File[] gameFolders = new File(musicFolder).listFiles(p1 -> p1.isDirectory() && p1.getName().startsWith("th"));

    public MusicRecongnition(BotWrapper botHelper) {
        super(botHelper);
        for (File gameFolder : gameFolders) {
            String[] list = gameFolder.list((p1, p2) -> p2.endsWith(".mp3"));
            if (list != null) {
                Collections.addAll(musicNames, list);
            }
        }
    }

    @Override
    public String getModuleName() {
        return "原曲认知";
    }

    @Override
    @CommandDescribe(cmd = "原曲认知", note = "可通过添加后缀E N H L来调节难度")
    public boolean onGroupMessage(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        long group = event.getGroup().getId();
        long qq = event.getSender().getId();
        if (msg.startsWith(".原曲认知")) {
            UserInfoManager.getInstance().incQaCount(qq);
            Iterator<String> iterator = TextLexer.analyze(msg).iterator();
            iterator.next();
            iterator.next();
            int needSeconds = 3;
            if (iterator.hasNext()) {
                SecondaryCommand secondaryCommand = Command.touHouMusicTest.getSecondaryCommand(iterator.next());
                switch (secondaryCommand) {
                    case music_test_easy:
                        needSeconds = 10;
                        break;
                    case music_test_normal:
                        needSeconds = 5;
                        break;
                    case music_test_hard:
                        needSeconds = 3;
                        break;
                    case music_test_lunatic:
                        needSeconds = 1;
                        break;
                }
            }

            File gameFolder = SJFRandom.randomSelect(gameFolders);
            File[] musics = gameFolder.listFiles(p1 -> p1.getName().endsWith(".mp3"));
            if (musics == null) {
                sendQuote(event, "暂未添加相关内容");
                return true;
            }
            File input = SJFRandom.randomSelect(musics);
            try {
                sendMessage(event, botWrapper.toAudio(generalCut(input, SJFPathTool.getMusicCutPath(System.currentTimeMillis() + "1.wav"), needSeconds), event.getGroup()));
            } catch (Exception e) {
                sendGroupMessage(group, e.toString());
                ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                return true;
            }
            QuestionAndAnswer.QABean bean = new QuestionAndAnswer.QABean();
            bean.fromQar = true;
            for (int i = 0; i < 3; ++i) {
                while (true) {
                    String musicName = SJFRandom.randomSelect(musicNames).replace("上海アリス幻樂団 - ", "").replace(".mp3", "");
                    if (!bean.answersToSelect.contains(musicName)) {
                        bean.answersToSelect.add(musicName);
                        break;
                    }
                }
            }
            int trueAnswer = SJFRandom.randomInt(4);
            bean.setTrueAns(trueAnswer);
            bean.answersToSelect.add(trueAnswer, input.getName().replace("上海アリス幻樂団 - ", "").replace(".mp3", ""));
            moduleManager.getModule(QuestionAndAnswer.class).addQuestion(qq, bean);
            StringBuilder builder = new StringBuilder();
            builder.append("名字是:\n");
            for (int i = 0; i < bean.answersToSelect.size(); ++i) {
                builder.append(i + 1).append(": ").append(bean.answersToSelect.get(i)).append("\n");
            }
            builder.append("回答序号即可");
            sendMessage(event, builder.toString());
            return true;
        }
        return false;
    }

    private File generalCut(File input, File output, int needSeconds) throws Exception {
        long audioStart = SJFRandom.nextInRange(0, AudioFileIO.read(input).getAudioHeader().getTrackLength() - needSeconds);
        Date start = new Date(audioStart * 1000 - 8 * 60 * 60 * 1000);
        Date end = new Date((audioStart + needSeconds) * 1000 - 8 * 60 * 60 * 1000);
        DateFormat fmt = new SimpleDateFormat("mm:ss");

        FfmpegCmdBuilder.AudioCommandBuilder builder = new FfmpegCmdBuilder.AudioCommandBuilder(input, output);
        //    builder.coverExistFile().author("SJF").comment("from 2un");
        builder.bitrate(64).freq(22050).channels(1).select("00:" + fmt.format(start), "00:" + fmt.format(end));
        String cmd = builder.build();
        try (CmdExecuter execute = CmdExecuter.execute(cmd, null)) {
            Process process = execute.getProcess();
            if (process != null && process.exitValue() == 0) {
                return output;
            }
        } catch (NullPointerException e) {
            if (botWrapper.debug) {
                e.printStackTrace();
            }
        }
        return null;
    }
}