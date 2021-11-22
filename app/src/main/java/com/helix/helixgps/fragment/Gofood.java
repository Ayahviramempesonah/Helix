package com.helix.helixgps.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helix.helixgps.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
 import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Gofood extends Fragment {

    private RecyclerView mRecyclerView;
    SwipeRefreshLayout swipe;
    View view;

    public Gofood() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_gf, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadOnHistory();
            }
        });

        LoadOnHistory();

        return view;
    }

    private void LoadOnHistory() {
        SessionManager sesi =new SessionManager(getContext());
        swipe.setRefreshing(true);

        ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseOnGoing> call = service.getListOrder(
                sesi.getKodplg(),
                "selesai","");

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
