package com.vastumbot.vastumap.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vastumbot.vastumap.databinding.FragmentGalleryBinding;
import com.vastumbot.vastumap.ui.Waste;
import com.vastumbot.vastumap.ui.home.HomeFragment;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private ArrayList<Waste> allWaste;

    private GalleryViewModel galleryViewModel;
    private FragmentGalleryBinding binding;
    private View aboutUsView;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        allWaste=HomeFragment.allWaste;
        
        return root;

    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}