package hoshin.autosell;

import java.util.List;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_465;
import net.minecraft.class_7923;

public class AutoSellLogic {
   private static int moveCount = 0;
   private static AutoSellLogic.State currentState = AutoSellLogic.State.STEP1_CHECK_GUI;
   private static AutoSellLogic.State nextState = AutoSellLogic.State.STEP1_CHECK_GUI;
   private static int waitTicks = 0;
   private static int successCount = 0;
   private static int noMoneyCount = 0;
   public static boolean receivedMoneyThisCycle = false;
   private static boolean pausedForBounty = false;
   private static boolean waitForClearScreen = false;
   private static class_1703 completedSaleHandler;
   private static int sleepTicks;
   private static final SellCommandPacing commandPacing = new SellCommandPacing();
   private static boolean reportedCommandTimeout;

   /** Only yield between complete sale cycles, never during slot moves or confirmation. */
   public static boolean canYieldForHome() {
      return !pausedForBounty && !waitForClearScreen
         && (currentState == State.STEP1_CHECK_GUI
            || currentState == State.SLEEPING && completedSaleHandler == null);
   }

   /** Drain only the outstanding synchronization delay when Control requests a stop.
    * Called once per client tick; never moves items or clicks a confirmation. */
   public static void tickRelease() {
      if (canYieldForHome()) return;
      if (currentState == State.WAITING_GUI && commandPacing.waiting(System.currentTimeMillis())) return;
      if (waitTicks > 0) { waitTicks--; return; }
      completedSaleHandler = null;
      currentState = State.STEP1_CHECK_GUI;
      pausedForBounty = waitForClearScreen = false;
   }

   public static void pauseForBounty() {
      pausedForBounty = true;
   }

   public static boolean isPausedForBounty() {
      return pausedForBounty;
   }

   public static void resumeAfterBounty() {
      if (pausedForBounty) {
         currentState = AutoSellLogic.State.STEP1_CHECK_GUI;
         nextState = AutoSellLogic.State.STEP1_CHECK_GUI;
         waitTicks = 10;
         moveCount = 0;
         receivedMoneyThisCycle = false;
         waitForClearScreen = true;
         pausedForBounty = false;
         completedSaleHandler = null;
         sleepTicks = 0;
      }
   }

   public static void reset() {
      currentState = AutoSellLogic.State.STEP1_CHECK_GUI;
      nextState = AutoSellLogic.State.STEP1_CHECK_GUI;
      waitTicks = 0;
      successCount = 0;
      noMoneyCount = 0;
      moveCount = 0;
      receivedMoneyThisCycle = false;
      completedSaleHandler = null;
      sleepTicks = 0;
      commandPacing.reset();
      reportedCommandTimeout = false;
   }

   private static boolean isItemInList(List<String> list, String itemId) {
      if (list != null && !list.isEmpty()) {
         for (String s : list) {
            String cleanS = s.toLowerCase().trim().replace("minecraft:", "");
            if (cleanS.equals(itemId)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void onTick(class_310 client) {
      if (client.field_1724 != null) {
         if (!pausedForBounty) {
            if (waitForClearScreen) {
               if (client.field_1755 instanceof class_465) {
                  return;
               }

               waitForClearScreen = false;
            }

            // Keep the same owned menu through confirmation, idle scans and long rests.
            // Only Control closes it when another automation actually needs the GUI.
            if (currentState == State.SLEEPING) {
               if (waitTicks > 0) {
                  waitTicks--;
                  return;
               }
               if (completedSaleHandler != null) {
                  if (!completedSaleHandler.method_34255().method_7960()) return;
                  completedSaleHandler = null;
                  moveCount = 0;
                  waitTicks = sleepTicks;
                  sleepTicks = 0;
                  return;
               }
               currentState = State.STEP1_CHECK_GUI;
            }

            if (ConfigManager.state == 2) {
               if (waitTicks > 0) {
                  waitTicks--;
               } else {
                  int filledSlots = 0;

                  for (int i = 0; i < 36; i++) {
                     if (!client.field_1724.method_31548().method_5438(i).method_7960()) {
                        filledSlots++;
                     }
                  }

                  if (filledSlots >= 36) {
                     ConfigManager.state = 1;
                     ConfigManager.save();
                     client.field_1724.method_7353(class_2561.method_43470("§a[auto sell] : đã có đủ item , tiếp tục bán"), false);
                     reset();
                  } else {
                     waitTicks = 10;
                  }
               }
            } else if (waitTicks > 0) {
               waitTicks--;
            } else {
               switch (currentState) {
                  case STEP1_CHECK_GUI:
                     if (!(client.field_1755 instanceof class_465)) {
                        if (client.field_1755 != null) return;
                        if (commandPacing.trySend(System.currentTimeMillis())) {
                           client.field_1724.field_3944.method_45730("sell");
                           currentState = State.WAITING_GUI;
                        } else if (!reportedCommandTimeout && commandPacing.exhausted(System.currentTimeMillis())) {
                           reportedCommandTimeout = true;
                           client.field_1724.method_7353(class_2561.method_43470("§e[AutoSell] Không nhận được menu sau 3 lần thử. Tắt/bật AutoSell để thử lại."), false);
                        }
                        break;
                     }

                     commandPacing.menuReceived(System.currentTimeMillis());
                     reportedCommandTimeout = false;
                     waitTicks = (int)(ConfigManager.delay_step * 20.0) + getRandomInt(1, 8);
                     moveCount = 0;
                     nextState = AutoSellLogic.State.STEP2_SCAN_INV;
                     currentState = AutoSellLogic.State.WAITING;
                     break;
                  case WAITING_GUI:
                     if (client.field_1755 instanceof class_465 || !commandPacing.waiting(System.currentTimeMillis())) {
                        currentState = State.STEP1_CHECK_GUI;
                     }
                     break;
                  case WAITING:
                     currentState = nextState;
                     break;
                  case STEP2_SCAN_INV:
                     if (client.field_1755 instanceof class_465 gui) {
                        class_1703 menu = gui.method_17577();
                        if (client.field_1724.field_7512 != menu || !menu.method_34255().method_7960()) return;
                        boolean foundItemToMove = false;
                        int itemsMovedThisTick = 0;
                        int BATCH_SIZE = ConfigManager.packet_size;
                        if (moveCount >= 36) {
                           waitTicks = (int)(ConfigManager.delay_step * 20.0) + getRandomInt(1, 8);
                           nextState = AutoSellLogic.State.STEP3_CONFIRM;
                           currentState = AutoSellLogic.State.WAITING;
                        } else {
                           for (class_1735 slotx : menu.field_7761) {
                              if (slotx.field_7871 == client.field_1724.method_31548() && slotx.method_7681()) {
                                 String itemId = class_7923.field_41178.method_10221(slotx.method_7677().method_7909()).method_12832();
                                 if ((!ConfigManager.enable_whitelist || isItemInList(ConfigManager.whitelist, itemId))
                                    && (!ConfigManager.enable_blacklist || !isItemInList(ConfigManager.blacklist, itemId))) {
                                    client.field_1761.method_2906(menu.field_7763, slotx.field_7874, 0, class_1713.field_7794, client.field_1724);
                                    foundItemToMove = true;
                                    moveCount++;
                                    itemsMovedThisTick++;
                                    if (moveCount >= 36) {
                                       break;
                                    }

                                    if (itemsMovedThisTick >= BATCH_SIZE) {
                                       waitTicks = ConfigManager.delay_move;
                                       currentState = AutoSellLogic.State.WAITING_MOVE;
                                       break;
                                    }
                                 }
                              }
                           }

                           if (!foundItemToMove || itemsMovedThisTick < BATCH_SIZE) {
                              if (foundItemToMove) {
                                 waitTicks = ConfigManager.delay_move;
                                 currentState = AutoSellLogic.State.WAITING_MOVE;
                              } else {
                                 if (moveCount == 0) {
                                    // An idle scan is not a sale. Reuse the menu without an empty confirm.
                                    waitTicks = Math.max(20, (int)(ConfigManager.delay_step * 20.0));
                                    currentState = State.SLEEPING;
                                    break;
                                 }
                                 waitTicks = (int)(ConfigManager.delay_step * 20.0) + getRandomInt(1, 8);
                                 nextState = AutoSellLogic.State.STEP3_CONFIRM;
                                 currentState = AutoSellLogic.State.WAITING;
                              }
                           }
                        }
                     } else {
                        moveCount = 0;
                        waitTicks = (int)(ConfigManager.delay_step * 20.0);
                        nextState = AutoSellLogic.State.STEP1_CHECK_GUI;
                        currentState = AutoSellLogic.State.WAITING;
                     }
                     break;
                  case WAITING_MOVE:
                     currentState = AutoSellLogic.State.STEP2_SCAN_INV;
                     break;
                  case STEP3_CONFIRM:
                     if (moveCount == 0) { currentState = State.STEP1_CHECK_GUI; break; }
                     if (client.field_1755 instanceof class_465 gui) {
                        class_1703 menu = gui.method_17577();
                        if (client.field_1724.field_7512 != menu || !menu.method_34255().method_7960()) return;
                        boolean confirmed = false;

                        for (class_1735 slot : menu.field_7761) {
                           if (slot.field_7871 != client.field_1724.method_31548()
                              && slot.method_7677().method_31574(class_1802.field_8581)) {
                              client.field_1761.method_2906(menu.field_7763, slot.field_7874, 0, class_1713.field_7790, client.field_1724);
                              confirmed = true;
                              break;
                           }
                        }

                        waitTicks = (int)(ConfigManager.delay_step * 20.0) + getRandomInt(1, 8);
                        if (!confirmed) break;
                        completedSaleHandler = menu;
                        sleepTicks = 0;
                        currentState = State.SLEEPING;
                        if (!receivedMoneyThisCycle) {
                           noMoneyCount++;
                           if (noMoneyCount >= 36) {
                              ConfigManager.state = 2;
                              ConfigManager.save();
                              client.field_1724.method_7353(class_2561.method_43470("§6[auto sell]: nghi ngờ hết item. Đã tạm dừng"), false);
                              waitTicks = Math.max(waitTicks, 10);
                              return;
                           }
                        } else {
                           noMoneyCount = 0;
                        }

                        receivedMoneyThisCycle = false;
                        successCount++;
                        if (successCount >= 10) {
                           successCount = 0;
                           client.field_1724.method_7353(class_2561.method_43470("§6[autosell]: tạm thời nghỉ để tránh sus"), false);
                           sleepTicks = (int)(ConfigManager.delay_sleep * 20.0) + getRandomInt(20, 200);
                        }

                     } else {
                        moveCount = 0;
                        waitTicks = (int)(ConfigManager.delay_step * 20.0);
                        nextState = AutoSellLogic.State.STEP1_CHECK_GUI;
                        currentState = AutoSellLogic.State.WAITING;
                     }
               }
            }
         }
      }
   }

   private static int getRandomInt(int min, int max) {
      return min + (int)(Math.random() * (max - min + 1));
   }

   private static enum State {
      STEP1_CHECK_GUI,
      WAITING_GUI,
      WAITING,
      STEP2_SCAN_INV,
      WAITING_MOVE,
      STEP3_CONFIRM,
      SLEEPING;
   }
}
