package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.EXVector3;

import java.util.*;

public class FXLightningBoltCommon {
    ArrayList<Segment> segments = new ArrayList<>();
    EXVector3 start;
    EXVector3 end;
    HashMap<Integer, Integer> splitparents = new HashMap<>();
    public float multiplier;
    public float length;
    public int numsegments0;
    public int increment;
    public int type = 0;
    public boolean nonLethal = false;
    private int numsplits;
    private boolean finalized;
    private Random rand;
    public long seed;
    public int particleAge;
    public int particleMaxAge;
    private Level world;
    public static final float speed = 3.0f;
    public static final int fadetime = 20;

    public FXLightningBoltCommon(Level world, EXVector3 jammervec, EXVector3 targetvec, long seed) {
        this.world = world;
        this.start = jammervec;
        this.end = targetvec;
        this.seed = seed;
        this.rand = new Random(seed);
        this.numsegments0 = 1;
        this.increment = 1;
        this.length = this.end.copy().sub(this.start).length();
        this.particleMaxAge = 3 + this.rand.nextInt(3) - 1;
        this.multiplier = 1.0f;
        this.particleAge = -((int)(this.length * 3.0f));
        this.segments.add(new Segment(this.start, this.end));
    }

    public FXLightningBoltCommon(Level world, Entity detonator, Entity target, long seed) {
        this(world, new EXVector3(detonator), new EXVector3(target), seed);
    }

    public FXLightningBoltCommon(Level world, Entity detonator, Entity target, long seed, int speed) {
        this(world,
                new EXVector3(detonator),
                new EXVector3(target.getX(),
                        target.getY() + target.getEyeHeight() - 0.7f,
                        target.getZ()),
                seed);
        this.increment = speed;
        this.multiplier = 0.4f;
    }

    public FXLightningBoltCommon(Level world, BlockEntity detonator, Entity target, long seed) {
        this(world, new EXVector3(detonator), new EXVector3(target), seed);
    }

    public FXLightningBoltCommon(Level world, BlockEntity detonator, double x, double y, double z, long seed) {
        this(world, new EXVector3(detonator), new EXVector3(x, y, z), seed);
    }

    public FXLightningBoltCommon(Level world, double x1, double y1, double z1,
                                 double x, double y, double z, long seed,
                                 int duration, float multi) {
        this(world, new EXVector3(x1, y1, z1), new EXVector3(x, y, z), seed);
        this.particleMaxAge = duration + this.rand.nextInt(duration) - duration / 2;
        this.multiplier = multi;
    }

    public FXLightningBoltCommon(Level world, double x1, double y1, double z1,
                                 double x, double y, double z, long seed,
                                 int duration, float multi, int speed) {
        this(world, new EXVector3(x1, y1, z1), new EXVector3(x, y, z), seed);
        this.particleMaxAge = duration + this.rand.nextInt(duration) - duration / 2;
        this.multiplier = multi;
        this.increment = speed;
    }

    public void setMultiplier(float m) {
        this.multiplier = m;
    }

    public void fractal(int splits, float amount, float splitchance,
                        float splitlength, float splitangle) {
        if (this.finalized) {
            return;
        }

        ArrayList<Segment> oldsegments = this.segments;
        this.segments = new ArrayList<>();
        Segment prev = null;

        for (Segment segment : oldsegments) {
            prev = segment.prev;
            EXVector3 subsegment = segment.diff.copy().scale(1.0f / (float)splits);
            BoltPoint[] newpoints = new BoltPoint[splits + 1];
            EXVector3 startpoint = segment.startpoint.point;

            newpoints[0] = segment.startpoint;
            newpoints[splits] = segment.endpoint;

            for (int i = 1; i < splits; ++i) {
                EXVector3 randoff = EXVector3.getPerpendicular(segment.diff)
                        .rotate(this.rand.nextFloat() * 360.0f, segment.diff);
                randoff.scale((this.rand.nextFloat() - 0.5f) * amount);

                EXVector3 basepoint = startpoint.copy().add(subsegment.copy().scale((float)i));
                newpoints[i] = new BoltPoint(basepoint, randoff);
            }

            for (int i = 0; i < splits; ++i) {
                Segment next = new Segment(
                        newpoints[i],
                        newpoints[i + 1],
                        segment.light,
                        segment.segmentno * splits + i,
                        segment.splitno
                );
                next.prev = prev;
                if (prev != null) {
                    prev.next = next;
                }

                if (i != 0 && this.rand.nextFloat() < splitchance) {
                    EXVector3 splitrot = EXVector3.xCrossProduct(next.diff)
                            .rotate(this.rand.nextFloat() * 360.0f, next.diff);
                    EXVector3 diff = next.diff.copy()
                            .rotate((this.rand.nextFloat() * 0.66f + 0.33f) * splitangle, splitrot)
                            .scale(splitlength);

                    ++this.numsplits;
                    this.splitparents.put(this.numsplits, next.splitno);

                    Segment split = new Segment(
                            newpoints[i],
                            new BoltPoint(newpoints[i + 1].basepoint,
                                    newpoints[i + 1].offsetvec.copy().add(diff)),
                            segment.light / 2.0f,
                            next.segmentno,
                            this.numsplits
                    );
                    split.prev = prev;
                    this.segments.add(split);
                }

                prev = next;
                this.segments.add(next);
            }

            if (segment.next != null) {
                segment.next.prev = prev;
            }
        }
        this.numsegments0 *= splits;
    }

    public void defaultFractal() {
        this.fractal(2, this.length * this.multiplier / 8.0f, 0.7f, 0.1f, 45.0f);
        this.fractal(2, this.length * this.multiplier / 12.0f, 0.5f, 0.1f, 50.0f);
        this.fractal(2, this.length * this.multiplier / 17.0f, 0.5f, 0.1f, 55.0f);
        this.fractal(2, this.length * this.multiplier / 23.0f, 0.5f, 0.1f, 60.0f);
        this.fractal(2, this.length * this.multiplier / 30.0f, 0.0f, 0.0f, 0.0f);
        this.fractal(2, this.length * this.multiplier / 34.0f, 0.0f, 0.0f, 0.0f);
        this.fractal(2, this.length * this.multiplier / 40.0f, 0.0f, 0.0f, 0.0f);
    }

    private void calculateCollisionAndDiffs() {
        HashMap<Integer, Integer> lastactivesegment = new HashMap<>();
        Collections.sort(this.segments, new SegmentSorter());

        int lastsplitcalc = 0;
        int lastactiveseg = 0;

        for (Segment segment : this.segments) {
            if (segment.splitno > lastsplitcalc) {
                lastactivesegment.put(lastsplitcalc, lastactiveseg);
                lastsplitcalc = segment.splitno;
                lastactiveseg = lastactivesegment.get(this.splitparents.get(segment.splitno));
            }
            lastactiveseg = segment.segmentno;
        }

        lastactivesegment.put(lastsplitcalc, lastactiveseg);
        lastsplitcalc = 0;
        lastactiveseg = lastactivesegment.get(0);

        Iterator<Segment> iterator = this.segments.iterator();
        while (iterator.hasNext()) {
            Segment segment = iterator.next();
            if (lastsplitcalc != segment.splitno) {
                lastsplitcalc = segment.splitno;
                lastactiveseg = lastactivesegment.get(segment.splitno);
            }
            if (segment.segmentno > lastactiveseg) {
                iterator.remove();
            } else {
                segment.calcEndDiffs();
            }
        }
    }

    public void finalizeBolt() {
        if (this.finalized) {
            return;
        }
        this.finalized = true;
        this.calculateCollisionAndDiffs();
        Collections.sort(this.segments, new SegmentLightSorter());
    }

    public void onUpdate() {
        this.particleAge += this.increment;
        if (this.particleAge > this.particleMaxAge) {
            this.particleAge = this.particleMaxAge;
        }
    }

    public class BoltPoint {
        EXVector3 point;
        EXVector3 basepoint;
        EXVector3 offsetvec;

        public BoltPoint(EXVector3 basepoint, EXVector3 offsetvec) {
            this.point = basepoint.copy().add(offsetvec);
            this.basepoint = basepoint;
            this.offsetvec = offsetvec;
        }
    }

    public class Segment {
        public BoltPoint startpoint;
        public BoltPoint endpoint;
        public EXVector3 diff;
        public Segment prev;
        public Segment next;
        public EXVector3 nextdiff;
        public EXVector3 prevdiff;
        public float sinprev;
        public float sinnext;
        public float light;
        public int segmentno;
        public int splitno;

        public Segment(BoltPoint start, BoltPoint end, float light,
                       int segmentnumber, int splitnumber) {
            this.startpoint = start;
            this.endpoint = end;
            this.light = light;
            this.segmentno = segmentnumber;
            this.splitno = splitnumber;
            this.calcDiff();
        }

        public Segment(EXVector3 start, EXVector3 end) {
            this(new BoltPoint(start, new EXVector3(0.0, 0.0, 0.0)),
                    new BoltPoint(end, new EXVector3(0.0, 0.0, 0.0)),
                    1.0f, 0, 0);
        }

        public void calcDiff() {
            this.diff = this.endpoint.point.copy().sub(this.startpoint.point);
        }

        public void calcEndDiffs() {
            EXVector3 thisdiffnorm;

            if (this.prev != null) {
                EXVector3 prevdiffnorm = this.prev.diff.copy().normalize();
                thisdiffnorm = this.diff.copy().normalize();
                this.prevdiff = thisdiffnorm.add(prevdiffnorm).normalize();
                this.sinprev = (float)Math.sin(
                        EXVector3.anglePreNorm(thisdiffnorm, prevdiffnorm.scale(-1.0f)) / 2.0f
                );
            } else {
                this.prevdiff = this.diff.copy().normalize();
                this.sinprev = 1.0f;
            }

            if (this.next != null) {
                EXVector3 nextdiffnorm = this.next.diff.copy().normalize();
                thisdiffnorm = this.diff.copy().normalize();
                this.nextdiff = thisdiffnorm.add(nextdiffnorm).normalize();
                this.sinnext = (float)Math.sin(
                        EXVector3.anglePreNorm(thisdiffnorm, nextdiffnorm.scale(-1.0f)) / 2.0f
                );
            } else {
                this.nextdiff = this.diff.copy().normalize();
                this.sinnext = 1.0f;
            }
        }

        @Override
        public String toString() {
            return this.startpoint.point.toString() + " " + this.endpoint.point.toString();
        }
    }

    public class SegmentLightSorter implements Comparator<Segment> {
        @Override
        public int compare(Segment o1, Segment o2) {
            return Float.compare(o2.light, o1.light);
        }
    }

    public class SegmentSorter implements Comparator<Segment> {
        @Override
        public int compare(Segment o1, Segment o2) {
            int comp = Integer.valueOf(o1.splitno).compareTo(o2.splitno);
            if (comp == 0) {
                return Integer.valueOf(o1.segmentno).compareTo(o2.segmentno);
            }
            return comp;
        }
    }
}
