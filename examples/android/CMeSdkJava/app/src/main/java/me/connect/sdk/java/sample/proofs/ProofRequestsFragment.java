package me.connect.sdk.java.sample.proofs;

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

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.databinding.ProofsFragmentBinding;

import static me.connect.sdk.java.sample.proofs.ProofCreateResult.FAILURE;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.FAILURE_CONNECTION;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.SUCCESS_CONNECTION;

public class ProofRequestsFragment extends Fragment {

    private ProofsFragmentBinding binding;
    private ProofRequestsViewModel model;

    public static ProofRequestsFragment newInstance() {
        return new ProofRequestsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ProofsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.requestsList.setLayoutManager(layoutManager);
        ProofRequestsAdapter adapter = new ProofRequestsAdapter(new ProofRequestsAdapter.ItemClickListener() {
            @Override
            public void onAcceptClick(int entryId) {
                accept(entryId);
            }

            @Override
            public void onRejectClick(int entryId) {
                reject(entryId);
            }
        });
        binding.requestsList.setAdapter(adapter);

        model = new ViewModelProvider(requireActivity()).get(ProofRequestsViewModel.class);
        model.getProofRequests().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonCheckRequests.setOnClickListener(v -> {
            binding.buttonCheckRequests.setEnabled(false);
            model.getNewProofRequests().observeOnce(getViewLifecycleOwner(), ok -> {
                binding.buttonCheckRequests.setEnabled(true);
            });
        });
    }

    private void accept(int proofId) {
        model.acceptProofRequest(proofId).observeOnce(getViewLifecycleOwner(), ok -> {
            switch (ok) {
                case SUCCESS:
                    Toast.makeText(getActivity(), "Proof request accepted", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(getActivity(), "Proof request accept failure", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS_CONNECTION:
                    Toast.makeText(getActivity(), "Connection created", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE_CONNECTION:
                    Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
                    break;
                case MISSED:
                    Toast.makeText(getActivity(), "Missed credential", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reject(int proofId) {
        model.rejectProofRequest(proofId).observeOnce(getViewLifecycleOwner(), ok -> {
            switch (ok) {
                case SUCCESS:
                    Toast.makeText(getActivity(), "Proof request rejected", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(getActivity(), "Proof request reject failure", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}

