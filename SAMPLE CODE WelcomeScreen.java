package ;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class WelcomeScreen extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int REQUEST_CHECK_SETTINGS = 111;
    private static final int REQUEST_READ_PHONE_STATE = 155;
    private static final boolean isApi23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static final boolean isApi29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    private Context context;
    private LocationManager locationManager;
    private Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        myActivity = this;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setContentView(R.layout.<Welcome Screen>);
        continue_clicked(null);

    }

    private void checkAndEnableGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(myActivity).checkLocationSettings(builder.build());
            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    resolvable.startResolutionForResult(myActivity, REQUEST_CHECK_SETTINGS);
                                } catch (Exception e) {
                                    Log.e("WelcomeScreen", e.getMessage());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                }
            });
        }
    }

    private boolean checkTelephonyPermission() {
        if (ContextCompat.checkSelfPermission(context, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(myActivity, new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                return false;
            } else {
                ActivityCompat.requestPermissions(myActivity, new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            }

            if (isApi23) myActivity.requestPermissions(new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (isApi29 && ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            checkLocationPermission();
                        }
                    }
                }
                checkTelephonyPermission();
                break;
            case REQUEST_READ_PHONE_STATE:
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (isApi23) {
                myActivity.requestPermissions(new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(myActivity, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(myActivity, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else if (isApi29 && ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, ACCESS_BACKGROUND_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.background_location_alert_title)
                        .setMessage(R.string.background_location_alert_text)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myActivity.requestPermissions(new String[]{ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .create().show();

            } else {
                myActivity.requestPermissions(new String[]{ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void continue_clicked(View view) {
        checkAndEnableGPS();
        boolean locationEnabled = checkLocationPermission();
        if (!locationEnabled)
            return;
        boolean telephonyEnabled = checkTelephonyPermission();
        if (!telephonyEnabled)
            return;

        if (locationEnabled && telephonyEnabled) {
            //Intent for Main Activity;
            Intent intent = new Intent(context, <Main Activity>);
            startActivity(intent);
            finishAffinity();
        }
    }
}