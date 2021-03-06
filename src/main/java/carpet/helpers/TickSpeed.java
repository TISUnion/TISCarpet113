package carpet.helpers;

import carpet.CarpetServer;
import carpet.logging.tickwarp.TickWarpHUDLogger;
import carpet.network.CarpetServerNetworkHandler;
import carpet.utils.Messenger;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

public class TickSpeed
{
    public static final int PLAYER_GRACE = 2;
    public static float tickrate = 20.0f;
    public static float mspt = 50.0f;
    public static long time_bias = 0;
    public static long time_warp_start_time = 0;
    public static long time_warp_scheduled_ticks = 0;
    public static EntityPlayer time_advancerer = null;
    public static String tick_warp_callback = null;
    public static CommandSource tick_warp_sender = null;
    public static int player_active_timeout = 0;
    public static boolean process_entities = true;
    public static boolean is_paused = false;
    public static boolean is_superHot = false;

    public static void reset_player_active_timeout()
    {
        if (player_active_timeout < PLAYER_GRACE)
        {
            player_active_timeout = PLAYER_GRACE;
        }
    }

    public static void reset()
    {
        tickrate = 20.0f;
        mspt = 50.0f;
        time_bias = 0;
        time_warp_start_time = 0;
        time_warp_scheduled_ticks = 0;
        time_advancerer = null;
        tick_warp_callback = null;
        tick_warp_sender = null;
        player_active_timeout = 0;
        process_entities = true;
        is_paused = false;
        is_superHot = false;
    }

    public static void add_ticks_to_run_in_pause(int ticks)
    {
        player_active_timeout = PLAYER_GRACE+ticks;
    }

    public static void tickrate(float rate) {tickrate(rate, true);}
    public static void tickrate(float rate, boolean update)
    {
        tickrate = rate;
        long mspt = (long)(1000.0f / tickrate);
        if (mspt <= 0L)
        {
            mspt = 1L;
            tickrate = 1000.0f;
        }

        TickSpeed.mspt = (float)mspt;

        if (update)
        {
            CarpetServerNetworkHandler.updateTickSpeedToConnectedPlayers();
        }
    }

    public static ITextComponent tickrate_advance(EntityPlayer player, int advance, String callback, CommandSource source)
    {
        if (0 == advance)
        {
            tick_warp_callback = null;
            tick_warp_sender = null;
            finish_time_warp();
            return Messenger.c("gi Warp interrupted");
        }
        if (time_bias > 0)
        {
            return Messenger.c("l Another player is already advancing time at the moment. Try later or talk to them");
        }
        time_advancerer = player;
        time_warp_start_time = System.nanoTime();
        time_warp_scheduled_ticks = advance;
        time_bias = advance;
        tick_warp_callback = callback;
        tick_warp_sender = source;

        // TISCM tickwarp logger
        TickWarpHUDLogger.getInstance().recordTickWarpAdvancer();

        return Messenger.c("gi Warp speed ....");
    }

    public static void finish_time_warp()
    {
        // TISCM tickwarp logger
        TickWarpHUDLogger.getInstance().recordTickWarpResult();

        long completed_ticks = time_warp_scheduled_ticks - time_bias;
        double milis_to_complete = System.nanoTime()-time_warp_start_time;
        if (milis_to_complete == 0.0)
        {
            milis_to_complete = 1.0;
        }
        milis_to_complete /= 1000000.0;
        int tps = (int) (1000.0D*completed_ticks/milis_to_complete);
        double mspt = (1.0*milis_to_complete)/completed_ticks;
        time_warp_scheduled_ticks = 0;
        time_warp_start_time = 0;
        if (tick_warp_callback != null)
        {
            Commands icommandmanager = tick_warp_sender.getServer().getCommandManager();
            try
            {
                int j = icommandmanager.handleCommand(tick_warp_sender, tick_warp_callback);

                if (j < 1)
                {
                    if (time_advancerer != null)
                    {
                        Messenger.m(time_advancerer, "r Command Callback failed: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                    }
                }
            }
            catch (Throwable var23)
            {
                if (time_advancerer != null)
                {
                    Messenger.m(time_advancerer, "r Command Callback failed - unknown error: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                }
            }
            tick_warp_callback = null;
            tick_warp_sender = null;
        }
        if (time_advancerer != null)
        {
            Messenger.m(time_advancerer, String.format("gi ... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
            time_advancerer = null;
        }
        else
        {
            Messenger.print_server_message(CarpetServer.minecraft_server, String.format("... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
        }
        time_bias = 0;

    }

    public static boolean continueWarp()
    {
        if (time_bias > 0)
        {
            if (time_bias == time_warp_scheduled_ticks) //first call after previous tick, adjust start time
            {
                time_warp_start_time = System.nanoTime();
            }
            time_bias -= 1;
            return true;
        }
        else
        {
            finish_time_warp();
            return false;
        }
    }

    public static void tick(MinecraftServer server)
    {
        process_entities = true;
        if (player_active_timeout > 0)
        {
            player_active_timeout--;
        }
        if (is_paused)
        {
            if (player_active_timeout < PLAYER_GRACE)
            {
                process_entities = false;
            }
        }
        else if (is_superHot)
        {
            if (player_active_timeout <= 0)
            {
                process_entities = false;

            }
        }


    }
}

