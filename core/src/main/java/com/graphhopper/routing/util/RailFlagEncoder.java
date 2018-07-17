package com.graphhopper.routing.util;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.PMap;

public class RailFlagEncoder extends AbstractFlagEncoder {

    private static final int DEFAULT_SPEED = 100;

    public RailFlagEncoder(PMap properties) {
        super(5, 5, 0);
        this.properties = properties;
        intendedValues.add("railway");
        intendedValues.add("train");
    }

    @Override
    public long acceptWay(ReaderWay way) {
        String railwayValue = way.getTag("railway");
        if (railwayValue == null) {
            if (way.hasTag("route", intendedValues)) {
                return acceptBit | ferryBit;
            }
            return 0;
        }

        if (!"rail".equals(railwayValue)) {
            return 0;
        }

        return acceptBit;
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        return oldRelationFlags;
    }

    @Override
    public String toString() {
        return "rail";
    }

    protected double getSpeed(ReaderWay way) {
        return DEFAULT_SPEED;
    }

    @Override
    public long handleNodeTags(ReaderNode node) {
        return 0;
    }

    @Override
    public long handleWayTags( ReaderWay way, long allowed, long relationCode )
    {
        if (!isAccept(allowed))
            return 0;

        long flags = 0;
        if (!isFerry(allowed)) {
            // get assumed speed
            double speed = getSpeed(way);
            speed = applyMaxSpeed(way, speed);

            flags = setSpeed(0, speed);

            if (way.hasTag("oneway", oneways) || way.hasTag("junction", "roundabout")) {
                if (way.hasTag("oneway", "-1"))
                    flags |= backwardBit;
                else
                    flags |= forwardBit;
            } else
                flags |= directionBitMask;

        } else {
            double ferrySpeed = getFerrySpeed(way);
            flags = setSpeed(flags, ferrySpeed);
            flags |= directionBitMask;
        }

        return flags;
    }

    @Override
    public int defineWayBits(int index, int shift) {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, DEFAULT_SPEED, DEFAULT_SPEED);
        return shift + speedBits;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
