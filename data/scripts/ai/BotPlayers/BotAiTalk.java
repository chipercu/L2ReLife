package ai.BotPlayers;

import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.serverpackets.Say2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;

public class BotAiTalk {
    private L2Character character;

    private boolean writeNewPattern = false;
    private String learnUnknownPattern;

    static String[] COMMON_PHRASES = {
            "Нет ничего ценнее слов, сказанных к месту и ко времени.",
            "Порой молчание может сказать больше, нежели уйма слов.",
            "Перед тем как писать/говорить всегда лучше подумать.",
            "Вежливая и грамотная речь говорит о величии души.",
            "Приятно когда текст без орфографических ошибок.",
            "Многословие есть признак неупорядоченного ума.",
            "Слова могут ранить, но могут и исцелять.",
            "Записывая слова, мы удваиваем их силу.",
            "Кто ясно мыслит, тот ясно излагает.",
            "Боюсь Вы что-то не договариваете."};
    static String[] ELUSIVE_ANSWERS = {
            "Вопрос непростой, прошу тайм-аут на раздумья.",
            "Не уверен, что располагаю такой информацией.",
            "Может лучше поговорим о чём-то другом?",
            "Простите, но это очень личный вопрос.",
            "Не уверен, что Вам понравится ответ.",
            "Поверьте, я сам хотел бы это знать.",
            "Вы действительно хотите это знать?",
            "Уверен, Вы уже догадались сами.",
            "Зачем Вам такая информация?",
            "Давайте сохраним интригу?"};
    static Map<String, String> PATTERNS_FOR_ANALYSIS = new HashMap<String, String>() {{
        // hello
        put("reload_patern", "reload_patern");
        put("save_patern", "save_patern");
        put("хай", "hello");
        put("привет", "hello");
        put("здорово", "hello");
        put("здравствуй", "hello");
        // who
        put("кто\\s.*ты", "who");
        put("ты\\s.*кто", "who");
        // name
        put("как\\s.*зовут", "name");
        put("как\\s.*имя", "name");
        put("есть\\s.*имя", "name");
        put("какое\\s.*имя", "name");
        // howareyou
        put("как\\s.*дела", "howareyou");
        put("как\\s.*жизнь", "howareyou");
        // whatdoyoudoing
        put("зачем\\s.*тут", "whatdoyoudoing");
        put("зачем\\s.*здесь", "whatdoyoudoing");
        put("что\\s.*делаешь", "whatdoyoudoing");
        put("чем\\s.*занимаешься", "whatdoyoudoing");
        // whatdoyoulike
        put("что\\s.*нравится", "whatdoyoulike");
        put("что\\s.*любишь", "whatdoyoulike");
        // iamfeelling
        put("кажется", "iamfeelling");
        put("чувствую", "iamfeelling");
        put("испытываю", "iamfeelling");
        // yes
        put("^да", "yes");
        put("согласен", "yes");
        // whattime
        put("который\\s.*час", "whattime");
        put("сколько\\s.*время", "whattime");

        //server
        put("как сервер", "server");
        // bye
        put("прощай", "bye");
        put("увидимся", "bye");
        put("до\\s.*свидания", "bye");
    }};
    static Map<String, String> ANSWERS_BY_PATTERNS = new HashMap<String, String>() {{
        put("hello", "Здравствуйте, рад Вас видеть.");
        put("who", "Я обычный чат-бот.");
        put("name", "Зови меня)");
        put("howareyou", "Спасибо, что интересуетесь. У меня всё хорошо.");
        put("whatdoyoudoing", "Я пробую общаться с людьми.");
        put("whatdoyoulike", "Мне нравиться думать что я не просто программа.");
        put("iamfeelling", "Как давно это началось? Расскажите чуть подробнее.");
        put("yes", "Согласие есть продукт при полном непротивлении сторон.");
        put("bye", "До свидания. Надеюсь, ещё увидимся.");
        put("server", "Обалденный, что то свежее в линейке");
    }};

    public BotAiTalk(L2Character character) {
        this.character = character;
        loadAnswers_byPatterns();
    }




    private void saveNewAnswer(String msg) {
        Properties properties = new Properties();
        properties.put(learnUnknownPattern, msg);
        try {
            properties.store(Files.newOutputStream(Paths.get("Answers.properties"), StandardOpenOption.APPEND), "byFuzzY");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void saveNewPaterns() {
//        Properties properties = new Properties();
//        properties.putAll(PATTERNS_FOR_ANALYSIS);


        FileWriter file = null;
        StringBuilder sb = new StringBuilder();
        try {
            file = new FileWriter("Patterns.txt", true);
            sb.append(learnUnknownPattern + ":" + learnUnknownPattern + "\n");
            //dropItem(activeChar);
            file.write(sb.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert file != null;
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



//        try {
//            properties.store(Files.newOutputStream(Paths.get("Patterns.properties"), StandardCharsets.UTF_8), null);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void loadAnswers_byPatterns() {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get("Answers.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String key : properties.stringPropertyNames()) {
            ANSWERS_BY_PATTERNS.put(key, properties.get(key).toString());

            //L2ObjectsStorage.getPlayer("WishMaster").sendMessage(key + ":" + properties.get(key).toString());
        }
    }

    private void loadPatterns() {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get("Patterns.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String key : properties.stringPropertyNames()) {
            PATTERNS_FOR_ANALYSIS.put(key, properties.get(key).toString());
        }
    }

    public String sayInReturn(String msg, boolean ai, L2Character player, int _chatType) {

        Pattern pattern; // for regexp
        Random random = new Random(); // for random answers
        Date date = new Date(); // for date and time

        String say;

        if (writeNewPattern){
            ANSWERS_BY_PATTERNS.put(learnUnknownPattern, msg);
            saveNewAnswer(msg);
            writeNewPattern = false;
            //saveNewAnswer();
            player.sendMessage("запись добавлена");
            return ":)";

        }
        if (msg.trim().endsWith("?")) {
            return ELUSIVE_ANSWERS[random.nextInt(ELUSIVE_ANSWERS.length)];
        } else if (msg.trim().endsWith("loadanswers")) {
            loadAnswers_byPatterns();
            return  "паттерны успешно загружены";
        }
//        else {
//            return COMMON_PHRASES[random.nextInt(COMMON_PHRASES.length)];
//        }

        if (!ANSWERS_BY_PATTERNS.containsKey(msg)){
            writeNewPattern = true;
            //PATTERNS_FOR_ANALYSIS.put(msg, "unknown");
            //saveNewPaterns();
            learnUnknownPattern = msg;
            player.sendMessage("начет процесс определения");
            return  learnUnknownPattern;
            //player.sendPacket(new Say2(character.getObjectId(), _chatType, "->" + character.getName(), learnUnknownPattern));

//            ThreadPoolManager.getInstance().schedule(new Runnable() {
//                @Override
//                public void run() {
//                    player.sendPacket(new Say2(character.getObjectId(), _chatType, "->" + character.getName(), learnUnknownPattern));
//                }
//            }, 1500);


        }


        if (ai) {
            String message =
                    String.join(" ", msg.toLowerCase().split("[ {,|.}?]+"));
            for (Map.Entry<String, String> o : PATTERNS_FOR_ANALYSIS.entrySet()) {
                pattern = Pattern.compile(o.getKey());
                if (pattern.matcher(message).find()) {
                    if (o.getValue().equals("whattime")) {
                        return date.toString();
                    } else if (o.getValue().equals("name")) {
                        return "Зови меня " + character.getName();
                    } else if (o.getValue().equals("save_patern")) {
                        //saveNewAnswer();
                        saveNewPaterns();
                        return "Список паттернов сохранен";
                    } else {
                        return ANSWERS_BY_PATTERNS.get(o.getValue());
                    }
                }
            }
        }







        //return say;
        return ANSWERS_BY_PATTERNS.get(msg);
    }


}
