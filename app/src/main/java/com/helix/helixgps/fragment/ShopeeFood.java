package com.helix.helixgps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.helix.helixgps.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopeeFood extends Fragment {

    public ShopeeFood() {
    }

    RelativeLayout relativeLayout;
    private RecyclerView mRecyclerView;
    SwipeRefreshLayout swipe;
    EditText textCari;
    RelativeLayout cari_no;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_ongoing, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        textCari = (EditText) view.findViewById(R.id.et_cari);
        cari_no = (RelativeLayout) view.findViewById(R.id.cari_no);

        cari_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadOnGoingData(textCari.getText().toString().trim());
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadOnGoingData(textCari.getText().toString().trim());
            }
        });

        LoadOnGoingData(textCari.getText().toString().trim());

        relativeLayout = (RelativeLayout)view.findViewById(R.id.relative);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OngoingDetail.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void LoadOnGoingData(String textCari) {

        SessionManager sesi =new SessionManager(getContext());
        swipe.setRefreshing(true);

        if (!textCari.equals("")){

            ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseOnGoing> call = service.getListOrder(
                    sesi.getKodplg(),
                    "ongoing",
                    textCari);

            call.enqueue(new Callback<ResponseOnGoing>() {
                @Override
                public void onResponse(Call<ResponseOnGoing> call, Response<ResponseOnGoing> response) {
                    swipe.setRefreshing(false);
                    if (response.isSuccessful()){
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        OnGOingAdapter onGOingAdapter = new OnGOingAdapter(response, getActivity(),"History");
                        mRecyclerView.setAdapter(onGOingAdapter);
                        mRecyclerView.invalidate();
                        swipe.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseOnGoing> call, Throwable t) {
                    swipe.setRefreshing(false);
                    HeroHelper.alert(getContext(), "", "Silahkan periksa kembali Internet anda");
                }
            });

        }else {
            ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseOnGoing> call = service.getListOrder(
                    sesi.getKodplg(),
                    "ongoing",
                    "");

            call.enqueue(new Callback<ResponseOnGoing>() {
                @Override
                public void onResponse(Call<ResponseOnGoing> call, Response<ResponseOnGoing> response) {
                    swipe.setRefreshing(false);
                    if (response.isSuccessful()){
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        OnGOingAdapter onGOingAdapter = new OnGOingAdapter(response, getActivity(),"History");
                        mRecyclerView.setAdapter(onGOingAdapter);
                        mRecyclerView.invalidate();
                        swipe.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseOnGoing> call, Throwable t) {
                    swipe.setRefreshing(false);
                    HeroHelper.alert(getContext(), "", "Silahkan periksa kembali Internet anda");
                }
            });
        }


    }
}
