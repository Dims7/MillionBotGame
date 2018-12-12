import java.util.ArrayList;
import java.util.HashMap;

public class MainProcessor {
    //ToDO вынести в класс DaTa всё, что относиттся к состоянию игры
    private IOMultiplatformProcessor ioMultiplatformProcessor = new IOMultiplatformProcessor();
    private UsersDataBase usersDataBase = new UsersDataBase(ioMultiplatformProcessor);
    private RegistartionProcessor registartionProcessor = new RegistartionProcessor(ioMultiplatformProcessor, usersDataBase);
    private ArrayList<Lobby> lobbyes = new ArrayList<>();
    private HashMap<String, Integer> usersGamesForTags = new HashMap<String, Integer>();
    private MenuProcessor menuProcessor = new MenuProcessor(ioMultiplatformProcessor, lobbyes, usersDataBase);
    private ArrayList<GameFortune> games = new ArrayList<GameFortune>();

    public void start() {
        while (true) {
            processFinishedGames();
            tryStartLobbyes();
            //ToDo перенести в добавление пользователя
            if (ioMultiplatformProcessor.isHasUnprocessedRequests()) {
                Request request = ioMultiplatformProcessor.pollRequest();
                if (usersDataBase.isUserNotRegister(request.getUser())) { //Незарегистрированный
                    registartionProcessor.processRequest(request);
                } else if (!usersGamesForTags.containsKey(request.getUser().getTag())) { //Не в игре
                    menuProcessor.processRequest(request);
                } else
                    games.get(usersGamesForTags.get(request.getUser().getTag())).processRequest(request);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryStartLobbyes() {
        for (int i = 0; i < lobbyes.size(); ++i) {
            if (lobbyes.get(i) != null && lobbyes.get(i).isGameCanStart()) {
                lobbyes.get(i).startGame();
                for (User user: lobbyes.get(i).usersInLobby) {
                    usersGamesForTags.put(user.getTag(), i);
                }
                GameFortune game = new GameFortune(lobbyes.get(i).usersInLobby, ioMultiplatformProcessor);
                games.add(game);
                game.start();
            }
        }
    }

    private void processFinishedGames() {
        for (int i = 0; i < games.size(); ++i) {
            if (games.get(i) != null && games.get(i).isGameFinished()) {
                games.set(i, null);
                for (User user : lobbyes.get(i).getUsersInLobby())
                    usersGamesForTags.remove(user.getTag());
                lobbyes.set(i, null);
            }
        }
    }
}