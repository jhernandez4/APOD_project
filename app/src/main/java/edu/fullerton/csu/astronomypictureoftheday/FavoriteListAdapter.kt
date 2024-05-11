package edu.fullerton.csu.astronomypictureoftheday

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.fullerton.csu.astronomypictureoftheday.databinding.ListItemFavoriteBinding
import java.util.Calendar

class FavoriteHolder (private val binding: ListItemFavoriteBinding)
    : RecyclerView.ViewHolder(binding.root){
        fun bind(favorite: Favorite, onCrimeClicked: (favoriteDate: String) -> Unit) {
            val dateFormatted = formatDate(favorite.date)
            binding.favoriteTitle.text = favorite.title
            binding.favoriteDate.text = dateFormatted
            binding.root.setOnClickListener{
                onCrimeClicked(dateFormatted)
            }
        }

    fun formatDate(date: Calendar) : String{
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)

        return "$month/$day/$year"
    }


}
class FavoriteListAdapter(
    private val favorites: List<Favorite>,
    private val onCrimeClicked: (favoriteDate: String) -> Unit
) : RecyclerView.Adapter<FavoriteHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFavoriteBinding.inflate(inflater, parent, false)
        return FavoriteHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteHolder, position: Int) {
        val favorite = favorites[position]
        holder.bind(favorite, onCrimeClicked)
    }

    override fun getItemCount() = favorites.size
}