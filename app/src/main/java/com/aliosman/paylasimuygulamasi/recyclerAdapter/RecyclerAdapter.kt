package com.aliosman.paylasimuygulamasi.recyclerAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliosman.paylasimuygulamasi.databinding.RecyclerViewBinding
import com.aliosman.paylasimuygulamasi.model.SharingModel
import com.squareup.picasso.Picasso

class RecyclerAdapter(private val sharingList: ArrayList<SharingModel>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(val binding: RecyclerViewBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return sharingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtUserName.text = sharingList.get(position).userName
        holder.binding.txtDescription.text = sharingList.get(position).description
        holder.binding.txtPaylasimTarihi.text = sharingList.get(position).time
        holder.binding.txtPaylasimKonumu.text = sharingList.get(position).location
        Picasso.get().load(sharingList.get(position).url).into(holder.binding.Image)
    }
}