package com.projectrestaurant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.projectrestaurant.databinding.FragmentMainBinding
import com.projectrestaurant.ui.order.ActivityOrder

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val MILLISECONDS_PER_IMAGE = 5000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageIds: HashSet<Int> = getImagesIdByName()
        var imageView: ImageView
        for(id in imageIds) {
            imageView = ImageView(this.context)
            imageView.setImageResource(id)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.viewFlipperMain.addView(imageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        binding.viewFlipperMain.flipInterval = MILLISECONDS_PER_IMAGE
        binding.cardViewNewOrder.setOnClickListener{ startActivity(Intent(requireActivity(), ActivityOrder::class.java)) }
    }

    override fun onStart() {
        super.onStart()
        binding.viewFlipperMain.startFlipping()
    }

    override fun onStop() {
        super.onStop()
        binding.viewFlipperMain.stopFlipping()
    }

    @SuppressLint("DiscouragedApi")
    private fun getImagesIdByName(str: String = "restaurant"): HashSet<Int> {
        val set = HashSet<Int>()
        val res = R.drawable::class.java.fields
        for (r in res) {
            if(r.name.startsWith(str, true))
                set.add(resources.getIdentifier(r.name, "drawable", this.context?.packageName))
        }
        return set
    }
}
