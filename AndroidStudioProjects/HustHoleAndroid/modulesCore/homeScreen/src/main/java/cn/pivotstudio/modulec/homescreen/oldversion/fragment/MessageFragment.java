//package cn.pivotstudio.modulec.homescreen.oldversion.fragment;
//
//
//import static cn.pivotstudio.modulec.homescreen.oldversion.message.ParseNotificationData.parseJson;
//import static cn.pivotstudio.modulec.homescreen.oldversion.message.ParseNotificationData.parseSysJson;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//
//import com.alibaba.android.arouter.launcher.ARouter;
//import com.cdh.okone.OkOne;
//import cn.pivotstudio.moduleb.libbase.constant.Constant;
//import com.scwang.smart.refresh.layout.api.RefreshLayout;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//
//import cn.pivotstudio.moduleb.database.MMKVUtil;
//import cn.pivotstudio.modulec.homescreen.BuildConfig;
//import cn.pivotstudio.modulec.homescreen.R;
//import cn.pivotstudio.modulec.homescreen.oldversion.message.NotificationAdapter;
//import cn.pivotstudio.modulec.homescreen.oldversion.message.NotificationBean;
//import cn.pivotstudio.modulec.homescreen.oldversion.message.SystemNotification;
//import cn.pivotstudio.modulec.homescreen.oldversion.message.SystemNotificationBean;
//import cn.pivotstudio.modulec.homescreen.oldversion.model.CheckingToken;
//import cn.pivotstudio.modulec.homescreen.oldversion.model.MessageStandardRefreshFooter;
//import cn.pivotstudio.modulec.homescreen.oldversion.model.StandardRefreshHeader;
//import cn.pivotstudio.modulec.homescreen.oldversion.network.RetrofitManager;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//
//public class MessageFragment extends Fragment {
//    private static List<NotificationBean> myNotificationList = new ArrayList<>();
//    private static Boolean isNotification = false;
//    private NotificationAdapter adapter;
//    private RecyclerView notificationRecyclerView;
//    private final static String TAG = "tag";
//    private ImageView noNotificationImage;
//    private TextView thereIsNoNotification;
//    private int page = 1;
//    private final static int list_size = 15;
//    private int start_id = 0;
//    private boolean finishRefresh = false;
//    private boolean isRefreshing = false;
//    private boolean isOnLoadMore = false;
//    private boolean finishOnLoadMore = false;
//
//    private Boolean isAll = false;
//    private Boolean hasInit = false;
//
//    public String url = RetrofitManager.API + "notices/";
//    private String token;
//
//
//    private List<SystemNotificationBean> mSystemNotificationList = new ArrayList<>();
//    private TextView latestSystemNotification;
//    private String mSystemMessage = null;
//    private boolean getMySystemMessage = false;
//
//    private int latestSystemNotificationPriority = 0;
//    private int getNotificationPriority = 1;
//
//    public static MessageFragment newInstance() {
//        MessageFragment fragment = new MessageFragment();
//        return fragment;
//    }
//
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_notice, container, false);
//        RefreshLayout refreshLayout = rootView.findViewById(R.id.refreshLayout);
//        refreshLayout.setRefreshHeader(new StandardRefreshHeader(getActivity()));
//        refreshLayout.setRefreshFooter(new MessageStandardRefreshFooter(getActivity()));
//
//        refreshLayout.setEnableLoadMore(true);
//        refreshLayout.setEnableRefresh(true);
//        //refreshLayout.setEnableScrollContentWhenLoaded(false);//??????????????????????????????????????????????????????
//        //refreshLayout.setEnableAutoLoadMore(false);//????????????????????????????????????????????????????????????
//
//        refreshLayout.setOnRefreshListener(refreshlayout -> {
//            if (CheckingToken.IfTokenExist()) {
//                //  Log.d(TAG, "onCreateView: here are refresh");
//                if (page > 1) {
//                    myNotificationList.clear();
//                    //mSystemNotificationList.clear();
//                    adapter.getSystemNotification(null);
//                    page = 1;
//                }
//                isAll = false;
//                hasInit = false;
//                start_id = 0;
//                isRefreshing = true;
//                getLatestSystemNotification();
//                getStringByOkhttp(url + "?" + "start_id=" + start_id + "&" +
//                        "list_size=" + list_size);
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        /**
//                         *??????????????????
//                         */
//                        if (finishRefresh) {
//                            refreshlayout.finishRefresh();
//                            isRefreshing = false;
//                            finishRefresh = false;
//                        } else {
//                            refreshlayout.autoRefresh();
//                        }
//                    }
//                }, 500);//0.5????????????Runnable??????run??????
//                refreshlayout.finishRefresh(1500/*,false*/);//??????false??????????????????
//            } else {
//                refreshLayout.finishRefresh();
//            }
//
//        });
//        refreshLayout.setOnLoadMoreListener(refreshlayout -> {
//            if (CheckingToken.IfTokenExist()) {
//                if (!isAll) {
//                    getStringByOkhttp(url + "?" + "start_id=" + start_id + "&" +
//                            "list_size=" + list_size);
//                    isOnLoadMore = true;
//                    Handler handler = new Handler();
//                    handler.postDelayed(() -> {
//                        /**
//                         *??????????????????
//                         */
//                        if (finishOnLoadMore) {
//                            refreshlayout.finishLoadMore();
//                            isOnLoadMore = false;
//                            finishOnLoadMore = false;
//                            adapter.notifyDataSetChanged();
//                        } else {
//                            refreshlayout.autoLoadMore();
//                        }
//                    }, 500);
//                } else {
//                    refreshlayout.finishLoadMoreWithNoMoreData();
//                }
//                refreshlayout.finishLoadMore(1500/*,false*/);//??????false??????????????????
//            } else {
//                refreshlayout.finishLoadMore();
//            }
//
//        });
//
////        noNotificationImage = rootView.findViewById(R.id.no_notification_image);
////        thereIsNoNotification = rootView.findViewById(R.id.there_is_no_notification);
//        notificationRecyclerView = rootView.findViewById(R.id.rv_notices);
//        if (!hasInit) {
//            getStringByOkhttp(url + "?" + "start_id=" + start_id + "&" +
//                    "list_size=" + list_size);
//        } else if (myNotificationList != null && hasInit && page > 1) { //???????????????????????????,???????????????????????????????????????
//            noNotificationImage.setVisibility(View.GONE);
//            thereIsNoNotification.setVisibility(View.GONE);
//            adapter = new NotificationAdapter(requireActivity(), myNotificationList);
//            notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//            notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
//            notificationRecyclerView.setAdapter(adapter);
//            adapter.getSystemNotification(mSystemNotificationList.get(0).getSystemContent());
//            adapter.setOnItemClickListener(position -> {
//                if (position > 0) {
//                    startHoleActivity(position);
//                } else {
//                    Intent intent = new Intent(getActivity(), SystemNotification.class);
//                    startActivity(intent);
//                }
//            });
//        } else { //?????????null
//            noNotificationImage.setVisibility(View.VISIBLE);
//            thereIsNoNotification.setVisibility(View.VISIBLE);
//        }
//
//
//        getLatestSystemNotification();
//
//        return rootView;
//    }
//
//
//    public void getLatestSystemNotification() {
//        String mPath = RetrofitManager.API + "system_notices?start_id=0&list_size=1";
//
//        OkOne.enableRequestPriority(true);
//        OkHttpClient client = new OkHttpClient();
//        Message message = Message.obtain();
//
//        MMKVUtil mmkvUtil = MMKVUtil.getMMKV(getContext());
//        String token = mmkvUtil.getString("USER_TOKEN");
//        //  Log.d(TAG, "getStringByOkhttp: token "+token);
//        Request request = new Request.Builder().get().addHeader("Authorization", "Bearer " + token).url(mPath).build();
//
//        OkOne.setRequestPriority(request, latestSystemNotificationPriority);
//
//        //   Log.d(TAG, "getStringByOkhttp: request");
//        try {
//            Call call = client.newCall(request);
//            //  Log.d(TAG, "getStringByOkhttp: call");
//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    message.what = 1;
//                    message.obj = e.getMessage();
//                    //    Log.d(TAG, "onFailure: e.toString " + e.toString());
//                    //     Log.d(TAG, "e.getLocalizedMessage" + e.getLocalizedMessage());
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {//????????????????????????????????????
//                        //     Log.d(TAG, "?????????????????????");
//                        //     Log.d(TAG, "response.code()==" + response.code());
//                        final String responseData = response.body().string();
//                        //    Log.d(TAG, "response.body().string()== " + responseData);
//                        //     Log.d(TAG, "obtain message");
//                        String temp = responseData.replace("{\"system_notices\":", "");
//                        temp = removeCharAt(temp, temp.length() - 1);
//                        message.what = 0;
//                        message.obj = temp;
//                        systemNotificationHandler.sendMessage(message);
//                        //      Log.d(TAG, "onResponse: handler message");
//                    } else {
//                        message.what = 1;
//                        //      Log.d(TAG, "onResponse: response " + response.networkResponse());
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            //   Log.d(TAG, "getStringByOkhttp:i am exception, e.toString() " + e.toString());
//        }
//    }
//
//    private Handler systemNotificationHandler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            //  Log.d(TAG, "handleMessage: msr.what " +msg.what);
//            switch (msg.what) {
//                case 0://??????????????????
//                    String Data = (String) msg.obj;
//                    //    Log.d(TAG, "SystemNotificationhandleMessage: get Data "+ Data);
//                    try {
//                        mSystemNotificationList = parseSysJson(Data);
//                    } catch (Exception e) {
//
//                    }
//                    mSystemMessage = mSystemNotificationList.get(0).getSystemContent();
//                    getMySystemMessage = true;
//                    //  Log.d(TAG, "handleMessage: adapter.content"+mSystemNotificationList.get(0).getSystemContent());
//                    break;
//                case 1://??????
//                    break;
//            }
//        }
//    };
//
//
//    public void startHoleActivity(int mPosition) {
//        String data_hole_id = myNotificationList.get(mPosition - 1).getHole_id();//position-1?????????SystemNotification????????????item
//        if (BuildConfig.isRelease) {
//            ARouter.getInstance().build("/hole/HoleActivity")
//                    .withInt(Constant.HOLE_ID, Integer.valueOf(data_hole_id))
//                    .withBoolean(Constant.IF_OPEN_KEYBOARD, false)
//                    .navigation();
//        } else {
//            //????????????????????????
//        }
//    }
//
//    public static String removeCharAt(String s, int pos) {
//        return s.substring(0, pos) + s.substring(pos + 1);
//    }
//
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            Log.d(TAG, "handleMessage: msr.what " + msg.what);
//            switch (msg.what) {
//                case 0://????????????????????????
//                    String Data = (String) msg.obj;
//                    Data = Data.replace("{\"msg\":", "");
//                    Data = removeCharAt(Data, Data.length() - 1);
//                    Log.d(TAG, "handleMessage: get Data1 " + Data);
//                    myNotificationList = parseJson(Data);
//                    isNotification = true;
//                    if (isNotification) { //?????????????????????
//                        noNotificationImage.setVisibility(View.GONE);
//                        thereIsNoNotification.setVisibility(View.GONE);
//                        //Log.d(TAG, "handleMessage: size  " + myNotificationList.size());
//                        adapter = new NotificationAdapter(getActivity(), myNotificationList);
//                        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//                        notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
//                        notificationRecyclerView.setAdapter(adapter);
//                        if (getMySystemMessage) {
//                            adapter.getSystemNotification(mSystemNotificationList.get(0).getSystemContent());
//                            adapter.setMContent = true;
//                        } else {
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    /**
//                                     *??????????????????
//                                     */
//                                    adapter.getSystemNotification(mSystemMessage);
//                                }
//                            }, 500);
//                        }
//
//
//                        adapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
//                            @Override
//                            public void onClick(int position) {
//                                if (position > 0) {
//                                    startHoleActivity(position);
//                                    //Toast.makeText(getActivity(), "click " + position, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Intent intent = new Intent(getActivity(), SystemNotification.class);
//                                    startActivity(intent);
//                                }
//
//                            }
//                        });
//                        page++;
//                        start_id += list_size;
//                        Log.d(TAG, "handleMessage: start_id :" + start_id);
//                    } else {
//                        noNotificationImage.setVisibility(View.VISIBLE);
//                        thereIsNoNotification.setVisibility(View.VISIBLE);
//                        Log.d(TAG, "onCreateView: visible");
//                    }
//                    if (isRefreshing) {
//                        finishRefresh = true;
//                    }
//                    break;
//                case 1://??????
//                    thereIsNoNotification.setText("");
//                    if (isRefreshing) {
//                        finishRefresh = true;
//                    }
//                    if (isOnLoadMore) {
//                        finishOnLoadMore = true;
//                    }
//                    break;
//                case 2://????????????????????????
//                    String Data2 = (String) msg.obj;
//                    Data2 = Data2.replace("{\"msg\":", "");
//                    Data2 = removeCharAt(Data2, Data2.length() - 1);
//                    Log.d(TAG, "handleMessage: get Data2 " + Data2);
//                    myNotificationList.addAll(parseJson(Data2));
//                    adapter.getSystemNotification(mSystemNotificationList.get(0).getSystemContent());
//                    Log.d(TAG, "handleMessage: size  " + myNotificationList.size());
//
//                    /*Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            *//**
//                 *??????????????????
//                 *//*
//                            adapter.notifyDataSetChanged();
//                        }
//                    }, 400);//0.4????????????Runnable??????run??????*/
//
//                    Log.d(TAG, "handleMessage: case2");
//                    adapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
//                        @Override
//                        public void onClick(int position) {
//                            if (position > 0) {
//                                startHoleActivity(position);
//                                //Toast.makeText(getActivity(), "click " + position, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Intent intent = new Intent(getActivity(), SystemNotification.class);
//                                startActivity(intent);
//                            }
//
//                        }
//                    });
//                    page++;
//                    start_id += list_size;
//                    Log.d(TAG, "handleMessage: start_id :" + start_id);
//                    if (isRefreshing) {
//                        finishRefresh = true;
//                    }
//
//                    if (isOnLoadMore) {
//                        finishOnLoadMore = true;
//                    }
//                    break;
//
//                case 3:
//                    if (myNotificationList != null && page != 1) {
//                        /*adapter = new NotificationAdapter( getActivity(), myNotificationList);
//                        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity() , LinearLayoutManager.VERTICAL,false));
//                        notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
//                        notificationRecyclerView.setAdapter(adapter);*/
//                        adapter.notifyDataSetChanged();
//                        Log.d(TAG, "handleMessage: case3");
//                    } else {
//                        noNotificationImage.setVisibility(View.VISIBLE);
//                        thereIsNoNotification.setVisibility(View.VISIBLE);
//                        Log.d(TAG, "onCreateView: visible");
//                    }
//                    if (isRefreshing) {
//                        finishRefresh = true;
//                    }
//                    if (isOnLoadMore) {
//                        finishOnLoadMore = true;
//                    }
//                    break;
//            }
//        }
//    };
//
//    public void getStringByOkhttp(String path) {
//        OkOne.enableRequestPriority(true);
//        OkHttpClient client = new OkHttpClient();
//        Message message = Message.obtain();
//        MMKVUtil mmkvUtil = MMKVUtil.getMMKV(getContext());
//        String token = mmkvUtil.getString("USER_TOKEN");
//
//        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token).get().url(path).build();
//        OkOne.setRequestPriority(request, getNotificationPriority);
//
//
//        Log.d(TAG, "getStringByOkhttp: request ");
//        try {
//            Call call = client.newCall(request);
//            Log.d(TAG, "getStringByOkhttp: call");
//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    message.what = 1;
//                    message.obj = e.getMessage();
//                    System.out.println("Failed");
//                    Log.d(TAG, "onFailure: i am on Failure");
//                    Log.d(TAG, "onFailure: e.toString " + e.toString());
//                    Log.d(TAG, "e.getLocalizedMessage" + e.getLocalizedMessage());
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {//????????????????????????????????????
//                        Log.d(TAG, "?????????????????????");
//                        Log.d(TAG, "response.code()==" + response.code());
//                        final String responseData = response.body().string();
//                        Log.d(TAG, "response.body().string()== " + responseData);
//                        Log.d(TAG, "obtain message");
//
//                        String temp = responseData.replace("{\"msg\":", "");
//                        temp = temp.replace("}", "");
//                        if (temp.equals("null")) {  //????????????????????????????????????????????????????????????
//                            message.what = 3;
//                            isAll = true;
//                            hasInit = true;
//                        } else if (page == 1) {
//                            message.what = 0;
//                            hasInit = true;
//                        } else if (page > 1) {
//                            message.what = 2;
//                        }
//                        message.obj = responseData;
//                        handler.sendMessage(message);
//                        Log.d(TAG, "onResponse: handler message");
//                    } else {
//                        Log.d(TAG, "onResponse: response " + response.networkResponse());
//                    }
//                }
//            });
//        } catch (Exception e) {
//            Log.d(TAG, "getStringByOkhttp: i am exception");
//            e.printStackTrace();
//            Log.d(TAG, "getStringByOkhttp: e.toString() " + e.toString());
//        }
//        Log.d(TAG, "getStringByOkhttp: getResponseData: null ");
//    }
//}
//
//
