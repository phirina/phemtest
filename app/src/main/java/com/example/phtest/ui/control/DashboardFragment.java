package com.example.phtest.ui.control;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.phtest.MainActivity;
import com.example.phtest.R;
import com.example.phtest.ui.bluetooth.HomeFragment;

import java.util.Objects;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    HomeFragment activity;
    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView  textView3, textView5;
    Button stop_btn, jump_btn;

    JoyStickClass js;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_control, container, false);

        textView3 = root.findViewById(R.id.joystick_angle);
        textView5 = root.findViewById(R.id.joystick_direction);
        jump_btn = root.findViewById(R.id.jump_btn);
        stop_btn = root.findViewById(R.id.stop_btn);

        layout_joystick = root.findViewById(R.id.layout_joystick);

        js = new JoyStickClass(root.getContext().getApplicationContext()
                , layout_joystick, R.drawable.image_button_v2);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));

                    int direction = js.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        textView5.setText("Direction : Up");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if(direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView3.setText("Angle :");
                    textView5.setText("Direction :");
                }
                return true;
            }
        });


        stop_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                activity = (HomeFragment) getTargetFragment();
                HomeFragment fragment = (HomeFragment)getParentFragment();
                if((fragment.getmConnectedThread() != null)) //First check to make sure thread created
                fragment.getmConnectedThread().write("1");
            }
        });

        jump_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });
        return root;
    }
}