package me.connect.sdk.java.sample.credentials;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.connect.sdk.java.sample.databinding.CredentialsFragmentBinding;

import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.FAILURE;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.FAILURE_CONNECTION;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.SUCCESS_CONNECTION;

public class CredentialOffersFragment extends Fragment {

    private CredentialsFragmentBinding binding;
    private CredentialOffersViewModel model;

    public static CredentialOffersFragment newInstance() {
        return new CredentialOffersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = CredentialsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.credentialsList.setLayoutManager(layoutManager);
        CredentialOffersAdapter adapter = new CredentialOffersAdapter(offerId -> {
            accept(offerId);
        });
        binding.credentialsList.setAdapter(adapter);


        model = new ViewModelProvider(requireActivity()).get(CredentialOffersViewModel.class);
        model.getCredentialOffers().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonCheckOffers.setOnClickListener(v -> {
            binding.buttonCheckOffers.setEnabled(false);
            model.getNewCredentialOffers().observeOnce(getViewLifecycleOwner(), ok -> {
                binding.buttonCheckOffers.setEnabled(true);
            });
        });
    }

    private void accept(int offerId) {
        model.acceptOffer(offerId).observeOnce(getViewLifecycleOwner(), ok -> {
            switch (ok) {
                case SUCCESS:
                    Toast.makeText(getActivity(), "Accept offer processed", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(getActivity(), "Accept offer failure", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS_CONNECTION:
                    Toast.makeText(getActivity(), "Connection created", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE_CONNECTION:
                    Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
