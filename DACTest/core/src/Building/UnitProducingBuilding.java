package building;

import Enums.BuildingType;
import Enums.UnitType;
import game.map.Map;
import Units.OffensiveUnit;
import Units.Unit;

import java.awt.*;

/**
 * Created by Daniel on 26-3-2017.
 */
public class UnitProducingBuilding extends Building {



    public UnitProducingBuilding(Point coordinate, int sizeX, int sizeY, BuildingType buildingType, int health)
    {
        this.setCoordinate(coordinate);
        this.setSizeX(sizeX);
        this.setSizeY(sizeY);
        this.setBuildingType(buildingType);
        this.setHealth(health);
    }

    public Unit produceUnit(int unitId, UnitType unittype, Map map){
        Point point = new Point(getCoordinate().x,getCoordinate().y - 1);
        Unit unit;
        switch (unittype) {
            case Knight:
                unit = new OffensiveUnit(unitId, point, UnitType.Knight, 100, 1.5, 1, 10, 1, true, map);
                break;
            case PikeMan:
                unit = new OffensiveUnit(unitId, point, UnitType.PikeMan, 80, 1.3, 1, 8, 1, true, map);
                break;
            case Archer:
                unit = new OffensiveUnit(unitId, point, UnitType.Archer, 60, 1, 1, 8, 4, true, map);
                break;
            case Builder:
                unit = new OffensiveUnit(unitId, point, UnitType.Builder, 50, 1.2, 0, 0, 0, false, map);
                break;
            default:
                throw new IllegalArgumentException("default switchcase reached in unitProducing building : produceUnit");
        }
        map.getTileFromCord(point).setUnit(unit);
        return unit;
    }
}
