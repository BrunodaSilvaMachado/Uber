package com.cursoandroid.uber.helper;

import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.cursoandroid.uber.R;

public class Permissoes {

    @NonNull
    public static ActivityResultLauncher<String[]> validarPermissoes(Activity activity, @NonNull AlertDialog dialog){
        return ((AppCompatActivity)activity).registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), resultsMap -> {
                    for (Boolean garanted : resultsMap.values()) {
                        if (!garanted) {
                            dialog.show();
                        }
                    }
                }
        );
    }

    @NonNull
    public static ActivityResultLauncher<String[]> validarPermissoes(Activity activity){
        final AlertDialog alertDefault = new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.msg_request_permission)
                .setPositiveButton(R.string.accept, (d, w) -> activity.finish())
                .setCancelable(false).create();
        return validarPermissoes(activity, alertDefault);
    }
}
