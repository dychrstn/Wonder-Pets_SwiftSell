package com.example.scanit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(private val productList: List<Product>): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        val decodedImage = decodeImage(product.itemImage)
        holder.imageViewProduct.setImageBitmap(decodedImage)

        // Set the string values to the TextViews
        holder.textViewName.text = product.itemName
        holder.textViewPrice.text = product.itemPrice.toString()
        holder.textViewQuantity.text = product.itemQuantity.toString()

    }

    private fun decodeImage(imageString: String): Bitmap?{
        val decodedBytes = Base64.decode(imageString, Base64.DEFAULT)
        if (decodedBytes != null && decodedBytes.isNotEmpty()) {
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
        return null
    }


}