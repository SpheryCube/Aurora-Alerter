package org.mssm.httpwww.auroraalerter_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by daniel14 on 10/10/16.
 */


public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("Lifecycle", "I'm in onReceive of AlarmReceiver because I'm cool");
        //Goal: Launch service from here whenever we recieve an alarm message

        Intent service_intent = new Intent(context, AuroraService.class);

        service_intent.putExtras(intent.getExtras());

        context.startService(service_intent);
    }
}
