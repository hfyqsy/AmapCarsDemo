package com.hfjs.amapcardemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hfjs.amapcardemo.R;
import com.hfjs.amapcardemo.adapter.ClassAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends PermissionsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_class);
        List<Class> classList = new ArrayList<>();
        classList.add(MapLineActivity.class);
        classList.add(SingerCarMapActivity.class);
        classList.add(ClusterActivity.class);
        ClassAdapter adapter = new ClassAdapter(R.layout.item_text, classList);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            startActivity(MainActivity.this, (Class) adapter1.getData().get(position));
        });



//        PhoneInfo siminfo = new PhoneInfo(MainActivity.this);
//        System.out.println("getProvidersName:"+siminfo.getProvidersName());
//        System.out.println("getNativePhoneNumber:"+siminfo.getNativePhoneNumber());
//        System.out.println("------------------------");
//        System.out.println("getPhoneInfo:"+siminfo.getPhoneInfo());
    }

    private void startActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity, aClass);
        startActivity(intent);
    }
}


