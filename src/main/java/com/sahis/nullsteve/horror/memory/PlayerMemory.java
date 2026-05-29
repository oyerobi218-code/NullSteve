package com.sahis.nullsteve.horror.memory;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
* Per-player remembered positions. Bounded sizes to stay cheap.
*/
public final class PlayerMemory {
   private static final int MAX_DOORS = 16;
   private static final int MAX_WINDOWS = 24;
   private static final int MAX_CHESTS = 24;
   private static final int MAX_ROUTE = 64;

   private @Nullable BlockPos bedPos;
   private @Nullable BlockPos basePos;

   private final LinkedHashSet<BlockPos> doors = new LinkedHashSet<>();
   private final LinkedHashSet<BlockPos> windows = new LinkedHashSet<>();
   private final LinkedHashSet<BlockPos> chests = new LinkedHashSet<>();
   private final ArrayDeque<BlockPos> route = new ArrayDeque<>();

   private int horrorStage = 1;        // 1..4
   private long eventBudgetTicks = 0;  // cooldown gate

   public @Nullable BlockPos getBedPos() { return bedPos; }
   public void setBedPos(BlockPos p) { bedPos = p; }
   public @Nullable BlockPos getBasePos() { return basePos; }
   public void setBasePos(BlockPos p) { basePos = p; }

   public Set<BlockPos> getDoorPositions() { return Collections.unmodifiableSet(doors); }
   public void rememberDoor(BlockPos p) { addBounded(doors, p, MAX_DOORS); }
   public Set<BlockPos> getWindowPositions() { return Collections.unmodifiableSet(windows); }
   public void rememberWindow(BlockPos p) { addBounded(windows, p, MAX_WINDOWS); }
   public Set<BlockPos> getChestPositions() { return Collections.unmodifiableSet(chests); }
   public void rememberChest(BlockPos p) { addBounded(chests, p, MAX_CHESTS); }

   public void appendRoute(BlockPos p) {
       if (route.peekLast() != null && route.peekLast().getSquaredDistance(p) < 4) return;
       route.addLast(p);
       while (route.size() > MAX_ROUTE) route.removeFirst();
   }

   public @Nullable BlockPos randomDoor(Random rng) {
       if (doors.isEmpty()) return null;
       int idx = rng.nextInt(doors.size());
       int i = 0;
       for (BlockPos p : doors) {
           if (i++ == idx) return p;
       }
       return null;
   }

   public @Nullable BlockPos randomRoutePos(Random rng) {
       if (route.isEmpty()) return null;
       int idx = rng.nextInt(route.size());
       Iterator<BlockPos> it = route.iterator();
       for (int i = 0; i <= idx && it.hasNext(); i++) {
           BlockPos p = it.next();
           if (i == idx) return p;
       }
       return null;
   }

   public int getHorrorStage() { return horrorStage; }
   public void setHorrorStage(int s) { horrorStage = Math.max(1, Math.min(4, s)); }
   public void incrementStage() { if (horrorStage < 4) horrorStage++; }
   public void resetStages() { horrorStage = 1; eventBudgetTicks = 0; }

   public long getEventBudgetTicks() { return eventBudgetTicks; }
   public void setEventBudgetTicks(long t) { eventBudgetTicks = t; }

   public NbtCompound toNbt() {
       NbtCompound nbt = new NbtCompound();
       if (bedPos != null) nbt.putLong("bed", bedPos.asLong());
       if (basePos != null) nbt.putLong("base", basePos.asLong());
       nbt.put("doors", writePosList(doors));
       nbt.put("windows", writePosList(windows));
       nbt.put("chests", writePosList(chests));
       nbt.put("route", writePosList(route));
       nbt.putInt("stage", horrorStage);
       nbt.putLong("budget", eventBudgetTicks);
       return nbt;
   }

   public static PlayerMemory fromNbt(NbtCompound nbt) {
       PlayerMemory m = new PlayerMemory();
       if (nbt.contains("bed")) m.bedPos = BlockPos.fromLong(nbt.getLong("bed"));
       if (nbt.contains("base")) m.basePos = BlockPos.fromLong(nbt.getLong("base"));
       if (nbt.contains("doors")) readPosList(nbt.getList("doors", NbtElement.LONG_TYPE), p -> m.addBounded(m.doors, p, MAX_DOORS));
       if (nbt.contains("windows")) readPosList(nbt.getList("windows", NbtElement.LONG_TYPE), p -> m.addBounded(m.windows, p, MAX_WINDOWS));
       if (nbt.contains("chests")) readPosList(nbt.getList("chests", NbtElement.LONG_TYPE), p -> m.addBounded(m.chests, p, MAX_CHESTS));
       if (nbt.contains("route")) readPosList(nbt.getList("route", NbtElement.LONG_TYPE), m::appendRoute);
       if (nbt.contains("stage")) m.horrorStage = nbt.getInt("stage");
       if (nbt.contains("budget")) m.eventBudgetTicks = nbt.getLong("budget");
       return m;
   }

   private static NbtList writePosList(Collection<BlockPos> list) {
       NbtList out = new NbtList();
       for (BlockPos p : list) out.add(net.minecraft.nbt.NbtLong.of(p.asLong()));
       return out;
   }

   private static void readPosList(NbtList list, java.util.function.Consumer<BlockPos> consumer) {
       for (int i = 0; i < list.size(); i++) {
           net.minecraft.nbt.NbtElement el = list.get(i);
           if (el instanceof net.minecraft.nbt.NbtLong nl) {
               consumer.accept(BlockPos.fromLong(nl.longValue()));
           }
       }
   }

   private <T> void addBounded(LinkedHashSet<T> set, T item, int max) {
       if (set.contains(item)) {
           set.remove(item);
           set.add(item);
           return;
       }
       if (set.size() >= max) {
           Iterator<T> it = set.iterator();
           if (it.hasNext()) { it.next(); it.remove(); }
       }
       set.add(item);
   }
}
