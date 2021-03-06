package Player;

import game.TextureVault;
import Units.Unit;
import building.Building;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Daniel on 26-3-2017.
 */
public class Player implements Serializable {
    private int playerID;
    private String nickName;
    private int amountGold;
    private int amountWood;
    private int amountFood;
    private int amountStone;
    private ArrayList<Unit> units;
    private ArrayList<Building>buildings;
    private ArrayList<Unit> selectedUnits;
    private Building selectedBuilding;

    transient Logger LOGGER = Logger.getLogger(Player.class.getName());

    public Player(int playerID, String nickName) {
        this.playerID = playerID;
        this.nickName = nickName;
        this.amountGold = 500;
        this.amountWood = 500;
        this.amountFood = 500;
        this.amountStone = 500;
        this.units =  new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.selectedUnits = new ArrayList<>();
    }


    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = (ArrayList<Unit>) units;
    }

    public void addUnit(Unit unit) { this.units.add(unit);}

    public void removeUnit(Unit unit) {
        if (unit != null)
        {
            int count = -1;
            for(int i = 0 ; i < units.size(); ++i) {
                if(units.get(i).equals(unit)) {
                    count = i;
                }
                if(units.get(i) != null && units.get(i).getInBattleWith().equals(unit)){
                    units.get(i).setInBattleWith(null);
                }
            }
            if (count != -1) {
                units.remove(units.get(count));
                unit.getTile().setUnit(null);
            }
        }
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = (ArrayList<Building>) buildings;
    }

    public int getAmountGold() {
        return amountGold;
    }

    public void setAmountGold(int amountGold) {
        this.amountGold = amountGold;
    }

    public int getAmountWood() {
        return amountWood;
    }

    public void setAmountWood(int amountWood) {
        this.amountWood = amountWood;
    }

    public int getAmountFood() {
        return amountFood;
    }

    public void setAmountFood(int amountFood) {
        this.amountFood = amountFood;
    }

    public int getAmountStone() {
        return amountStone;
    }

    public void setAmountStone(int amountStone) {
        this.amountStone = amountStone;
    }


    public List<Unit> getSelectedUnits() {
        return selectedUnits;
    }

    public void setSelectedUnits(List<Unit> selectedUnits) {
        this.selectedUnits = (ArrayList<Unit>) selectedUnits;
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public void setSelectedBuilding(Building selectedBuilding) {
        this.selectedBuilding = selectedBuilding;
    }

    public void addUnitToSelectedUnits(Unit unit){
        this.selectedUnits.add(unit);
    }

    public void command(){

    }

    public boolean buyUnit(Unit unit){
        boolean canBuy = false;
        switch(unit.getUnitType())
        {
            case Knight:
                if(amountGold - 100 >= 0 && amountFood - 50 >= 0){
                    canBuy = true;
                    amountGold -= 100;
                    amountFood -= 50;
                }
                break;
            case Archer:
                if(amountGold - 50 >= 0 && amountFood - 35 >= 0 && amountWood - 25 >=0 ){
                    canBuy = true;
                    amountGold -= 50;
                    amountFood -= 35;
                    amountWood -= 25;
                }
                break;
            case PikeMan:
                if(amountGold - 20 >= 0 && amountFood - 50 >= 0 && amountWood - 25 >=0 ){
                    canBuy = true;
                    amountGold -= 20;
                    amountFood -= 50;
                    amountWood -= 25;
                }
                break;
            case Builder:
                if(amountFood - 100 >= 0){
                    canBuy = true;
                    amountFood -= 100;
                }
                break;
            default:
                return false;
        }

        if(canBuy){
            units.add(unit);
        }
        return canBuy;
    }

    public boolean buyBuilding(Building building){
        boolean canBuy = false;
        switch(building.getBuildingType())
        {
            case TownCenter:
                if(amountGold - 1000 >= 0 && amountFood - 1000 >= 0 && amountStone - 1000 >= 0){
                    canBuy = true;
                    amountGold -= 1000;
                    amountFood -= 100;
                    amountStone -= 1000;
                }
                break;
            case Archery:
                if(amountWood - 500 >= 0 && amountStone - 500 >=0 ){
                    canBuy = true;
                    amountStone -= 500;
                    amountWood -= 500;
                }
                break;
            default:
                return false;
        }

        if(canBuy){
            LOGGER.info("Can buy.");
            buildings.add(building);
        }
        return canBuy;
    }

    public void render(Batch batch){
        //TODO Check if this could be easier/more readable
        //render units
        for (int i = 0; i < units.size() && !units.isEmpty(); i++) {
            batch.draw(units.get(i).getSprite(), units.get(i).getPosition().x *16, units.get(i).getPosition().y*16, 16, 16);
            if (units.get(i).getHealth() > (int)(units.get(i).getMaxhealth()*(75.0f/100.0f))) {
                batch.draw(TextureVault.Health100, units.get(i).getPosition().x*16, units.get(i).getPosition().y*16, 16, 16);
            }
            if (units.get(i).getHealth() <= (int)(units.get(i).getMaxhealth()*(75.0f/100.0f)) && units.get(i).getHealth() > (int)(units.get(i).getMaxhealth()*(50.0f/100.0f))) {
                batch.draw(TextureVault.Health75, units.get(i).getPosition().x*16, units.get(i).getPosition().y*16, 16, 16);
            }
            if (units.get(i).getHealth() <= (int)(units.get(i).getMaxhealth()*(50.0f/100.0f)) && units.get(i).getHealth() > (int)(units.get(i).getMaxhealth()*(25.0f/100.0f))) {
                batch.draw(TextureVault.Health50, units.get(i).getPosition().x*16, units.get(i).getPosition().y*16, 16, 16);
            }
            if (units.get(i).getHealth() <= (int)(units.get(i).getMaxhealth()*(25.0f/100.0f))) {
                batch.draw(TextureVault.Health25, units.get(i).getPosition().x*16, units.get(i).getPosition().y*16, 16, 16);
            }
        }
        //render selected units
        for (int i = 0; i < selectedUnits.size(); i++) {
            batch.draw(selectedUnits.get(i).getSelectedSprite(), selectedUnits.get(i).getPosition().x*16, selectedUnits.get(i).getPosition().y*16, 16, 16);
        }

        //render buildings
        for (int i = 0; i < buildings.size() && !buildings.isEmpty(); i++) {
            batch.draw(buildings.get(i).getSprite(), buildings.get(i).getCoordinate().x*16, buildings.get(i).getCoordinate().y*16, buildings.get(i).getSizeX()*16, buildings.get(i).getSizeY()*16);
        }
        //render selected building
        if(selectedBuilding != null){
            batch.draw(selectedBuilding.getSelectedSprite(), selectedBuilding.getCoordinate().x*16, selectedBuilding.getCoordinate().y*16, selectedBuilding.getSizeX() *16, selectedBuilding.getSizeY()*16);
        }
    }
}
