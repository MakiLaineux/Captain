package com.technoprimates.captain.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.technoprimates.captain.StueckViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;

public class Utils {
    public static void loadAssets(Context context, StueckViewModel stueckViewModel) {
        String ligne; 		//ligne lue dans le fichier
        stueckViewModel.selectActionToProcess(Stueck.MODE_INSERT);

        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(context.getAssets().open("fill.txt"), "ISO-8859-1"));
            while ((ligne = buf.readLine()) != null) {
                if (ligne.length() <= Profile.NB_CHECKBOX) continue;
                String strProfile = ligne.substring(0,13);
                String strText = ligne.substring(13);

                Stueck stueck = new Stueck(strText, strProfile.replace('Y', ' '));
                Log.d("JCLOAD", "Stück ZZZ"+stueck.getName()+"ZZZ"+stueck.getProfile());
                stueckViewModel.selectStueckToProcess(stueck);
                stueckViewModel.insertStueck();
            }
            buf.close();

        } catch (IOException e) {
            Log.e("JCLOAD", "error");
        }
    }

}
