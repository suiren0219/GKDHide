package io.github.gkdhide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** 模块配置页：写入 SharedPreferences("config")，Hook 侧经 XSharedPreferences 读取 */
public class ConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        float d = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("GKD 隐身");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("LSPosed 模块：在作用域内的应用中隐藏 GKD 的无障碍服务状态，帮助通过无障碍检测。GKD 本体不受影响。\n\n"
                + "使用步骤：\n"
                + "1. 在 LSPosed 管理器中启用本模块；\n"
                + "2. 勾选需要过检测的应用作为作用域；\n"
                + "3. 强制停止目标应用后重新打开；\n"
                + "4. GKD 保持无障碍开启即可，无需其他配置。");
        desc.setPadding(0, (int) (10 * d), 0, (int) (10 * d));
        root.addView(desc);

        Switch swMaster = addSwitch(root, "总开关（关闭后不做任何隐藏）", sp.getBoolean("master", true));
        Switch swStrict = addSwitch(root, "完全伪装无障碍未开启（推荐；作用域应用会认为无障碍整体关闭）", sp.getBoolean("strict", true));
        Switch swHideInstalled = addSwitch(root, "隐藏已安装的无障碍服务列表（更彻底，但可能影响作用域应用自身的无障碍功能）", sp.getBoolean("hide_installed", false));
        Switch swLog = addSwitch(root, "输出日志（可在 LSPosed 日志中查看）", sp.getBoolean("log", false));

        root.addView(makeLabel("额外要隐藏的服务（可选；GKD 的 li.songe.gkd 始终包含），每行一个包名或组件ID："));
        EditText etTargets = makeInput(sp.getString("targets", ""));
        root.addView(etTargets);

        root.addView(makeLabel("排除的应用（这些应用内不做隐藏，每行一个包名）："));
        EditText etExcludes = makeInput(sp.getString("excludes", ""));
        root.addView(etExcludes);

        Button save = new Button(this);
        save.setText("保存");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit()
                        .putBoolean("master", swMaster.isChecked())
                        .putBoolean("strict", swStrict.isChecked())
                        .putBoolean("hide_installed", swHideInstalled.isChecked())
                        .putBoolean("log", swLog.isChecked())
                        .putString("targets", etTargets.getText().toString())
                        .putString("excludes", etExcludes.getText().toString())
                        .apply();
                Toast.makeText(ConfigActivity.this,
                        "已保存。配置约 3 秒内生效；若未生效，请强制停止目标应用后重开。",
                        Toast.LENGTH_LONG).show();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (16 * d);
        root.addView(save, lp);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private Switch addSwitch(LinearLayout root, String text, boolean checked) {
        Switch sw = new Switch(this);
        sw.setText(text);
        sw.setChecked(checked);
        root.addView(sw, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return sw;
    }

    private TextView makeLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, (int) (4 * getResources().getDisplayMetrics().density));
        return tv;
    }

    private EditText makeInput(String text) {
        EditText et = new EditText(this);
        et.setText(text);
        et.setMinLines(3);
        et.setGravity(android.view.Gravity.TOP);
        et.setTypeface(Typeface.MONOSPACE);
        et.setTextSize(13);
        return et;
    }
}
