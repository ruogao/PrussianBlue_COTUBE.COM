
package cotube.controller;


import com.amazonaws.util.IOUtils;
import cotube.domain.*;
import cotube.services.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64.Decoder;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping(value = "/createGame.html")
public class ajaxCreateGameController {

    private AccountService accountService;
    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    private ComicService comicService;
    @Autowired
    public void setComicService(ComicService comicService) {
        this.comicService = comicService;
    }

    private GameComicService gameComicService;
    @Autowired
    public void setGameComicService(GameComicService gameComicService) {
        this.gameComicService = gameComicService;
    }

    private PanelService panelService;
    @Autowired
    public void setPanelService(PanelService panelService) {
        this.panelService = panelService;
    }

    private KeywordService keywordService;
    @Autowired
    public void setKeywordService(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    private NotificationService notificationService;
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @RequestMapping(value = "/playRandom", method = RequestMethod.POST)
    @ResponseBody
    public String playRandom(HttpServletRequest request) throws IOException {
        String username = request.getParameter("username");
        Integer gameId = 0;
        Integer panelNo = 0;
        Random rand = new Random();
        List<GameComic> gcs = gameComicService.getAllGameComics();
        List<Integer> availableIds = new ArrayList<>();
        for(int i = 0; i < gcs.size(); i++) {
            GameComic gc = gcs.get(i);
            if (panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null)
                availableIds.add(gc.getGame_comic_id());
        }
        if (availableIds.size() > 0) {
            Integer num = rand.nextInt(gcs.size());
            Integer id = availableIds.get(num);
            Panel panel = new Panel();
            panel.setAuthor(username);
            gameId = id;
            panelNo = panel.getPanel_id();
            panel.setCanvas_path("gc_ " + id + "_panelID_" + panel.getPanel_id() + ".png");
            panelService.addPanel(panel);
            if (gameComicService.getGameComicByGameComicId(id).getPanel1_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel1_id(panel.getPanel_id());
            else if (gameComicService.getGameComicByGameComicId(id).getPanel2_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel2_id(panel.getPanel_id());
            else if (gameComicService.getGameComicByGameComicId(id).getPanel3_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel3_id(panel.getPanel_id());
            else
                gameComicService.getGameComicByGameComicId(id).setPanel4_id(panel.getPanel_id());
        }
        else {
            List<Keyword> keywords = keywordService.getAllKeywords();
            Integer num = rand.nextInt(keywords.size());
            String keyword = keywords.get(num).getKeyword();
            Panel panel = new Panel();
            panel.setAuthor(username);
            Comic comic = new Comic();
            comic.setComic_type(1);
            comic.setStatus(0);
            GameComic gc = new GameComic();
            gc.setGame_comic_id(comic.getComic_id());
            gc.setGamecomic_type(0);
            gc.setPanel1_id(panel.getPanel_id());
            gc.setKeyword(keyword);
            gc.setStatus(0); //remove status from gamecomic table in future
            panel.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel.getPanel_id() + ".png");
            panelService.addPanel(panel);
            comicService.addComic(comic);
            gameComicService.addGameComic(gc);
            gameId = comic.getComic_id();
            panelNo = panel.getPanel_id();
        }
        /*
            Definition of an avaliable game:
                A game which is not published and no other user is working on this game at the moment
                (Check the panels in the game, if there is a panel which author is not null and canvas_path is null,
                it means someone is working on the game)

            Find a random avaliable public game and assign the user to that game
            record the start time
            return the gameId and panelNo 

            If there are no game avaliable in database create a new one
            record start time

            DONE
        */

        System.out.println(username);
        System.out.println(gameId);
        System.out.println(panelNo);

        JSONObject result = new JSONObject();
        result.put("gameId",gameId);
        result.put("panelNo",panelNo);
        System.out.println(result.toString());
        return result.toString();
    }

    @RequestMapping(value = "/customCreate", method = RequestMethod.POST)
    @ResponseBody
    public String customCreate(HttpServletRequest request) throws IOException {
        String username = request.getParameter("username");
        String keyword = request.getParameter("keyword");
        Integer gameId = 0;
        Integer panelNo = 0;
        List<Keyword> keywords = keywordService.getAllKeywords();
        /*
            Create a new public game with the keyword

            Assign the user to the first panel of this game
            record start time

            return the gameId and panelNo 

            DONE
        */

        Panel panel = new Panel();
        panel.setAuthor(username);
        Comic comic = new Comic();
        comic.setComic_type(1);
        comic.setStatus(0);
        GameComic gc = new GameComic();
        gc.setGame_comic_id(comic.getComic_id());
        gc.setGamecomic_type(0);
        gc.setPanel1_id(panel.getPanel_id());
        gc.setKeyword(keyword);
        boolean flag = false;
        for (int i = 0; i < keywords.size(); i++){
            if (keywords.get(i).equals(keyword))
                flag = true;
        }
        if (flag == false){
            Keyword k = new Keyword();
            k.setKeyword(keyword);
            keywordService.addKeyword(k);
        }
        gc.setStatus(0); //remove status from gamecomic table in future
        panel.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel.getPanel_id() + ".png");
        panelService.addPanel(panel);
        comicService.addComic(comic);
        gameComicService.addGameComic(gc);
        gameId = comic.getComic_id();
        panelNo = panel.getPanel_id();


        System.out.println(username);
        System.out.println(keyword);
        System.out.println(gameId);
        System.out.println(panelNo);

        JSONObject result = new JSONObject();
        result.put("gameId",gameId);
        result.put("panelNo",panelNo);
        System.out.println(result.toString());
        return result.toString();
    }

    @RequestMapping(value = "/customExist", method = RequestMethod.POST)
    @ResponseBody
    public String customExist(HttpServletRequest request) throws IOException {
        String username = request.getParameter("username");
        String keyword = request.getParameter("keyword");
        Integer gameId = 0;
        Integer panelNo = 0;
        Boolean exist = false;
        Random rand = new Random();

        List<GameComic> gcs = gameComicService.getAllGameComics();
        List<Integer> sameKeywordIds = new ArrayList<>();
        for(int i = 0; i < gcs.size(); i++) {
            GameComic gc = gcs.get(i);
            if (gc.getKeyword().equals(keyword) && (gc.getGamecomic_type() == 0) &&
                    (panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null
                    || panelService.getPanelFromPanelId(gc.getPanel1_id()).getAuthor() == null))
                sameKeywordIds.add(gc.getGame_comic_id());
        }
        /*
            Search all the public games in the database by the given keyword

            If there is an avaliable game{
                assign the user into the game
                record start time
                set exist to true
                return the exist,gameId and panelNo 
            }else{
                set exist to false
                return the exist,gameId and panelNo
                (we will check the exist status in the page first, so the value of gameId and panelNo does not matter)
            }

            DONE
        */
        if (sameKeywordIds.size() > 0) {
            Integer num = rand.nextInt(gcs.size());
            Integer id = sameKeywordIds.get(num);
            Panel panel = new Panel();
            panel.setAuthor(username);
            gameId = id;
            panelNo = panel.getPanel_id();
            panel.setCanvas_path("gc_ " + id + "_panelID_" + panel.getPanel_id() + ".png");
            panelService.addPanel(panel);
            if (gameComicService.getGameComicByGameComicId(id).getPanel1_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel1_id(panel.getPanel_id());
            else if (gameComicService.getGameComicByGameComicId(id).getPanel2_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel2_id(panel.getPanel_id());
            else if (gameComicService.getGameComicByGameComicId(id).getPanel3_id() == null)
                gameComicService.getGameComicByGameComicId(id).setPanel3_id(panel.getPanel_id());
            else
                gameComicService.getGameComicByGameComicId(id).setPanel4_id(panel.getPanel_id());
            exist = true;
        }

        System.out.println(username);
        System.out.println(keyword);
        System.out.println(exist);
        System.out.println(gameId);
        System.out.println(panelNo);

        JSONObject result = new JSONObject();
        result.put("exist",exist);
        result.put("gameId",gameId);
        result.put("panelNo",panelNo);
        System.out.println(result.toString());
        return result.toString();
    }

    @RequestMapping(value = "/privateGame", method = RequestMethod.POST)
    @ResponseBody
    public String privateGame(HttpServletRequest request) throws IOException {
        String username = request.getParameter("username");
        String keyword = request.getParameter("keyword");
        String user2 = request.getParameter("user2");
        String user3 = request.getParameter("user3");
        String user4 = request.getParameter("user4");
        Integer gameId = 0;
        Integer panelNo = 0;
        if (this.accountService.usernameExist(user2) == true){
            Date now = new Date();
            int notification_type = 6;
            String notification = username + " has invited you to play in the comicGame";
            Notification note = new Notification();
            note.setNotifcation_type(notification_type);
            note.setNotification(notification);
            note.setUsername(user2);
            note.setNotifcation_time(now);
            note.setLink(gameId + " " + "2");
            this.notificationService.addNotification(note);
        }
        if (this.accountService.usernameExist(user3) == true){
            Date now = new Date();
            int notification_type = 6;
            String notification = username + " has invited you to play in the comicGame";
            Notification note = new Notification();
            note.setNotifcation_type(notification_type);
            note.setNotification(notification);
            note.setUsername(user3);
            note.setNotifcation_time(now);
            note.setLink(gameId + " " + "3");
            this.notificationService.addNotification(note);

        }
        if(this.accountService.usernameExist(user4) == true){
            Date now = new Date();
            int notification_type = 6;
            String notification = username + " has invited you to play in the comicGame";
            Notification note = new Notification();
            note.setNotifcation_type(notification_type);
            note.setNotification(notification);
            note.setUsername(user4);
            note.setNotifcation_time(now);
            note.setLink(gameId + " " + "4");
            this.notificationService.addNotification(note);
        }


        /*
            Create a new private game with the keyword

            Check if user2, user3 and user4 exist (There might be null or undefined user)
            Assign username to first panel, user2 to second panel, user3 to third panel, user4 to fourth panel
            Notify all the users in the game

            return the gameId and panelNo 

            DONE
        */


        Panel panel1 = new Panel();
        panel1.setAuthor(username);
        Panel panel2 = new Panel();
        panel2.setAuthor(user2);
        Panel panel3 = new Panel();
        panel3.setAuthor(user3);
        Panel panel4 = new Panel();
        panel4.setAuthor(user4);
        Comic comic = new Comic();
        comic.setComic_type(1);
        comic.setStatus(0);
        GameComic gc = new GameComic();
        gc.setGame_comic_id(comic.getComic_id());
        gc.setGamecomic_type(1);
        gc.setPanel1_id(panel1.getPanel_id());
        gc.setPanel2_id(panel2.getPanel_id());
        gc.setPanel3_id(panel3.getPanel_id());
        gc.setPanel4_id(panel4.getPanel_id());
        gc.setKeyword(keyword);
        List<Keyword> keywords = keywordService.getAllKeywords();
        boolean flag = false;
        for (int i = 0; i < keywords.size(); i++){
            if (keywords.get(i).equals(keyword))
                flag = true;
        }
        if (flag == false){
            Keyword k = new Keyword();
            k.setKeyword(keyword);
            keywordService.addKeyword(k);
        }
        gc.setStatus(0); //remove status from gamecomic table in future
        panel1.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel1.getPanel_id() + ".png");
        panel2.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel2.getPanel_id() + ".png");
        panel3.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel3.getPanel_id() + ".png");
        panel4.setCanvas_path("gc_ " + gc.getGame_comic_id() + "_panelID_" + panel4.getPanel_id() + ".png");
        panelService.addPanel(panel1);
        panelService.addPanel(panel2);
        panelService.addPanel(panel3);
        panelService.addPanel(panel4);
        comicService.addComic(comic);
        gameComicService.addGameComic(gc);
        gameId = comic.getComic_id();


        System.out.println(username);
        System.out.println(keyword);
        System.out.println(gameId);
        System.out.println(panelNo);

        JSONObject result = new JSONObject();
        result.put("gameId",gameId);
        result.put("panelNo",panelNo);
        System.out.println(result.toString());
        return result.toString();
    }

    @RequestMapping(value = "/getInfo", method = RequestMethod.POST)
    @ResponseBody
    public String getInfo(HttpServletRequest request) throws IOException {
        String gameID = request.getParameter("gameId");
        String cur = request.getParameter("current");
        Integer gameid = Integer.parseInt(gameID);
        Integer currentPanel = Integer.parseInt(cur);
        GameComic gc = gameComicService.getGameComicByGameComicId(gameid);
        List<String> path = new ArrayList<>();
        List<String> word = new ArrayList<>();
        List<String> num = new ArrayList<>();
        String keyword = gc.getKeyword();
        if(currentPanel==1){
            num.add("this");
            path.add(null);
            word.add(null);
        }else{
            num.add("No.1");
            if(gc.getPanel1_id()==null){
                path.add(null);
                word.add(null);
            }else{
                Panel temp = panelService.getPanelFromPanelId(gc.getPanel1_id());
                path.add(temp.getCanvas_path());
                word.add(temp.getTitle_word());
            }
        }
        if(currentPanel==2){
            num.add("this");
            path.add(null);
            word.add(null);
        }else{
            num.add("No.2");
            if(gc.getPanel2_id()==null){
                path.add(null);
                word.add(null);
            }else{
                Panel temp = panelService.getPanelFromPanelId(gc.getPanel2_id());
                path.add(temp.getCanvas_path());
                word.add(temp.getTitle_word());
            }
        }
        if(currentPanel==3){
            num.add("this");
            path.add(null);
            word.add(null);
        }else{
            num.add("No.3");
            if(gc.getPanel3_id()==null){
                path.add(null);
                word.add(null);
            }else{
                Panel temp = panelService.getPanelFromPanelId(gc.getPanel3_id());
                path.add(temp.getCanvas_path());
                word.add(temp.getTitle_word());
            }
        }
        if(currentPanel==4){
            num.add("this");
            path.add(null);
            word.add(null);
        }else{
            num.add("No.4");
            if(gc.getPanel4_id()==null){
                path.add(null);
                word.add(null);
            }else{
                Panel temp = panelService.getPanelFromPanelId(gc.getPanel4_id());
                path.add(temp.getCanvas_path());
                word.add(temp.getTitle_word());
            }
        }
        JSONObject result = new JSONObject();
        result.put("num", num);
        result.put("path", path);
        result.put("word", word);
        result.put("keyword", keyword);
        System.out.println(result.toString());
        return result.toString();
    }

    @RequestMapping(value = "/randomKeyword", method = RequestMethod.POST)
    @ResponseBody
    public String randomKeyword(HttpServletRequest request) {
        List<Keyword> keywords = keywordService.getAllKeywords();
        Random rand = new Random();
        Integer num = rand.nextInt(keywords.size());
        String keyword = keywords.get(num).getKeyword();
        /*

            SELECT A RANDOM KEYWORD FROM KEYWORD TABLE

                DONE
        */

        return keyword;
    }
}
