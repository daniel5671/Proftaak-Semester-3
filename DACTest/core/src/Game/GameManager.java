package game;

import building.Building;
import building.UnitProducingBuilding;
import Enums.BuildingType;
import Enums.State;
import game.map.Map;
import game.map.TiledMapStage;
import game.UserInterface.UIManager;
import multiplayer.GameManagerClient;
import Player.Player;
import Units.BuilderUnit;
import Units.OffensiveUnit;
import Units.Unit;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.OrthographicCameraControlClass;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Daniel on 26-3-2017.
 */

public class GameManager {
    private State gamestate;
    private int lobbyID;
    private String password;
    private Map map;
    private ArrayList<Player> players;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private Stage stage;
    private OrthographicCamera orthographicCamera;
    private GameManagerClient gmc;

    private int ownPlayerid;
    private UIManager uiManager;

    private OrthographicCameraControlClass gamecamera;
    //Stage en Skin voor UI inladen
    private SpriteBatch batch;

    public GameManager(State gameState, int lobbyID, String password, java.util.List<Player> players, int ownPlayerId) {
        this.gamestate = gameState;
        this.lobbyID = lobbyID;
        this.password = password;
        this.players = (ArrayList<Player>) players;
        this.ownPlayerid = ownPlayerId;
        this.gmc = new GameManagerClient(this);
    }

    public State getGamestate() {
        return this.gamestate;
    }

    public void setGamestate(State gamestate) {
        this.gamestate = gamestate;
    }

    public int getLobbyID() {
        return this.lobbyID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map getMap() {
        return this.map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public UIManager getUiManager() {
        return uiManager;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public GameManagerClient getGmc() {return gmc;}

    public void create() {
        // set camera
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, 1920, 1080);
        orthographicCamera.update();
        tiledMap = new TmxMapLoader().load("assets/TestMap3.tmx");
        map = new Map(tiledMap, "tmpNaam");
        gamecamera = new OrthographicCameraControlClass(800, tiledMap);

        //sets map
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        stage = new TiledMapStage(tiledMap, this);
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();

        //todo review of dit de correcte plek ervoor is.
        createTownCenters();
    }

    public void render() {
        //todo zet dit ergens anders. hoort op de tickrate te werken.
        //Yeah this shit needs fixing
        ArrayList<Unit> toRemoveUnits = new ArrayList<>();
      
        for (Player player : players) {
            List<Unit> units = player.getUnits();
            for (Unit unit : units) {
                if (unit.getDeltaMoveTime() > unit.getSpeed())
                {
                    unit.move(map);
                    unit.setDeltaMoveTime(0);
                }

                if(unit instanceof BuilderUnit){
                    //todo blame marc hiervoor
                    BuilderUnit bUnit = (BuilderUnit) unit;
                    if(bUnit.getPath().size() == 0){
                        if (bUnit.mineResource(bUnit.getResourceTile().getResource())){
                            //er word gemined
                        }else{
                            //er word niet gemined

                        }
                    }
                }
                //TODO Fix this mess
                if(unit instanceof OffensiveUnit) {
                    OffensiveUnit offunit = (OffensiveUnit) unit;
                    if (offunit.getInBattleWith() != null && offunit.getDeltaBattleTime() > 1) {
                        offunit.setDeltaBattleTime(0);
                        for (int x = 0; x < offunit.getHitPerSecond(); x++) {
                            offunit.attack(unit.getInBattleWith());
                            if (offunit.getHealth() <= 0) {
                                toRemoveUnits.add(offunit);
                            }
                            if (offunit.getInBattleWith().getHealth() <= 0) {
                                toRemoveUnits.add(offunit.getInBattleWith());
                            }
                        }
                    }
                    offunit.setDeltaBattleTime(offunit.getDeltaBattleTime() + Gdx.graphics.getDeltaTime());
                }
                unit.setDeltaMoveTime(unit.getDeltaMoveTime() + Gdx.graphics.getDeltaTime());
                map.setHostiles(unit);
            }
        }
        for (Player player : players)
        {
            ArrayList<Unit> selectedUnits = (ArrayList<Unit>) player.getSelectedUnits();
            for (Unit unit : toRemoveUnits)
            {
                player.removeUnit(unit);
                selectedUnits.remove(unit);
            }
            player.setSelectedUnits(selectedUnits);
        }

        renderCameraAndTiledMap();
        batch.begin();
        map.render(batch);
        for (Player player : players) {
            player.render(batch);
        }
        batch.end();
    }

    public Player getOwnPlayer() {
        return getPlayers().get(ownPlayerid);
    }

    private void renderCameraAndTiledMap() {
        orthographicCamera = gamecamera.render(orthographicCamera);
        orthographicCamera.update();
        tiledMapRenderer.render();
        stage.act();
        stage.draw();
        stage.getViewport().update((int) orthographicCamera.viewportWidth, (int) orthographicCamera.viewportHeight, false);
        stage.getViewport().setCamera(orthographicCamera);
        stage.getViewport().getCamera().update();
        batch.setProjectionMatrix(orthographicCamera.combined);
        tiledMapRenderer.setView(
                orthographicCamera.combined
                , 0
                , 0
                , tiledMap.getLayers().get(0).getProperties().get("width", Integer.class)//This works realy, really weird.
                , tiledMap.getLayers().get(0).getProperties().get("height", Integer.class)//This too
        );
    }

    private void createTownCenters(){
        for(int i=0; i<getPlayers().size(); i++){
            Point spawnPoint = map.getSpawnPoints().get(i);
            Point cord = map.getTileFromCord(spawnPoint).getCoordinate();
            Building townCenter = new UnitProducingBuilding(cord, 4, 4, BuildingType.TownCenter, 1000);
            if(map.checkBuildingPossible(townCenter)){
                map.setBuildingsTiles(townCenter);
                getPlayers().get(i).getBuildings().add(townCenter);
            }
        }
    }

    public int getHighestUnitId(){
        int highestUnitID = 0;
        for(Player player: getPlayers())
        {
            highestUnitID = highestUnitID + player.getUnits().size();
        }
        return highestUnitID;
    }

    public int getHighestBuildingId(){
        int highestBuildingID = 0;
        for(Player player: getPlayers())
        {
            highestBuildingID = highestBuildingID + player.getBuildings().size();
        }
        return highestBuildingID;
    }
}
