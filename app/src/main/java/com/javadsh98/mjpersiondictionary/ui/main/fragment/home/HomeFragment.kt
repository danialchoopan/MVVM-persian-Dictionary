package com.javadsh98.mjpersiondictionary.ui.main.fragment.home

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.javadsh98.mjpersiondictionary.R
import com.javadsh98.mjpersiondictionary.data.db.entity.Word
import com.javadsh98.mjpersiondictionary.databinding.FragmentHomeBinding
import com.javadsh98.mjpersiondictionary.ui.dialog.DetailCallback
import com.javadsh98.mjpersiondictionary.ui.dialog.DetailDialog
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment: Fragment(), SearchView.OnQueryTextListener, OnItemClickCallBack,
    DetailCallback {

    val RTL_CHARACTERS: Pattern =
        Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]")

    var binding: FragmentHomeBinding? = null
    lateinit var viewmodel: HomeViewModel
    lateinit var root: View
    var adapter: HomeAdapter? = null
    lateinit var detailDialog: DetailDialog

//    var searchWord: String = ""

    companion object{

        var instance: HomeFragment? = null

        fun newInstance(): HomeFragment{
            if(instance == null){
                instance = HomeFragment()
            }
            return instance!!
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        root = binding!!.root
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        setupRecycler()
        setupListener()
        getPreviousData()
    }

    fun getPreviousData() {

        viewmodel.getAllWord().let {
            if(it != null)
                it.observe(viewLifecycleOwner, Observer {
                    adapter!!.setList(it)
                    /*
                    if (it.isEmpty()) {
                        showMessage()
                    } else {
                        hideMessage()
                    }
                     */
                })
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(TextUtils.isEmpty(newText))
            getDefaultWord()

        return false
    }


    override fun onQueryTextSubmit(query: String?): Boolean {

        setSearchword(query!!)
        if(isPersian(query))
            searchPersian(query)
        else
            searchEnglish(query)
        root.searchview_home_search.clearFocus()

        return false
    }

    private fun searchPersian(query: String?) {

        viewmodel.searchPersian(query!!).observe(viewLifecycleOwner, Observer {
            if(isEqualeSearchWord())
                adapter!!.setList(it)
        })
    }

    private fun searchEnglish(query: String?) {

        viewmodel.searchEnglish(query!!).observe(viewLifecycleOwner, Observer {
            if (isEqualeSearchWord())
                adapter!!.setList(it)
        })
    }

    private fun setupRecycler() {

        adapter = HomeAdapter(this)

        root.recyclerview_home_words.setHasFixedSize(true)
        root.recyclerview_home_words.adapter = adapter

        var list = viewmodel.allWords.value
        if (!list!!.isEmpty()) {
            adapter!!.setList(list)
        } else {
            getDefaultWord()
        }


    }

    private fun setupListener() {
        root.searchview_home_search.setOnQueryTextListener(this)
    }

    override fun onLikeClick(word: Word) {
        viewmodel.update(word)
    }

    override fun onItemClick(word: Word) {
        detailDialog = DetailDialog.newInstance(word, !isPersian(getSearchWord()), adapter!!.words)
        detailDialog.retainInstance = true
        detailDialog.setCallBack(this)
        detailDialog.show(childFragmentManager, "detail-dialog")
    }

    override fun onMoreClick() {
        viewmodel.loadMore().observe(viewLifecycleOwner, Observer {
            adapter!!.setList(it)
        })
    }

    fun isPersian(str: String): Boolean{
        val matcher: Matcher = RTL_CHARACTERS.matcher(str)
        return if (matcher.find())  true else  false
    }

    fun setSearchword(searchWord: String){
        viewmodel.searchWord = searchWord
    }

    fun getSearchWord(): String = viewmodel.searchWord

    fun isEqualeSearchWord(): Boolean{
        return TextUtils.equals(getSearchWord(), searchview_home_search.query)
    }

    override fun onOkClicked(word: Word) {
        word.viewCount += 1
        viewmodel.update(word)
        detailDialog.dismiss()
    }

    override fun onNoClicked() {
        detailDialog.dismiss()
    }

    fun getDefaultWord() {
        viewmodel.getDefaultWord().observe(viewLifecycleOwner, Observer {
            adapter!!.setList(it)
        })
    }

    fun log(line: String){
        Log.d("DebugHomeFragment", "get default line ${line}")
    }

}
