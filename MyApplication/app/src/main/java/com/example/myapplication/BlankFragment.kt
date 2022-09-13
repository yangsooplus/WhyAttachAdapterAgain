package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView


class BlankFragment : Fragment() {
    lateinit var listData : MutableList<String>
    lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listData = mutableListOf("hello", "boostcamp", "K026")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.ListView)

        listAdapter = ListAdapter(requireContext(), listData)
        listView.adapter = listAdapter

        // 1 새 어댑터 새 데이터 -> notify 없이도 됨
        // 처음 리스트뷰를 생성한 것 처럼 새 데이터로 만들어진 어댑터로 갈아 끼워버림
        view.findViewById<Button>(R.id.NewNewButton).setOnClickListener {
            val newObjListAdapter = ListAdapter(requireContext(), listOf("legend", "of", "Zelda", "TearOfKingdom"))
            listView.adapter = newObjListAdapter
        }

        // 2 헌 어댑터 새 데이터 -> 한 번은 되는데 완전하지 못함
        view.findViewById<Button>(R.id.OldNewButton).setOnClickListener {
            /*
            // 2-1 되긴 됨, 근데 어댑터가 새로 생성되면 날아감
            listAdapter.changeDataListObject(listOf("kirby", "wii", "migration"))
            listAdapter.notifyDataSetChanged()
            */

            /*
            // 2-2 되긴 됨, 근데 2번을 누른 다음에 3번 실행하면 안됨.
            // 또 데이터를 바꾸고 싶으면 listData에 새로운 리스트 참조, 혹은 new_list의 데이터 변경해야 함
            val new_list = mutableListOf("kirby", "wii", "migration")
            listData = new_list
            listAdapter.notifyDataSetChanged()
            */

            /* 2-3 되긴 됨, 1번 갔다가 2번 돌아오는 것도 됨
            // 2-2처럼 3번을 실행하면 안됨. 이미 원래 listData를 참조를 잃어버려서
            // 1번의 어댑터를 불러와서 거기다가 새 리스트를 집어넣는 거니까
            val adapter = listView.adapter as ListAdapter
            adapter.changeDataListObject(listOf("kirby", "wii", "migration"))
            adapter.notifyDataSetChanged()
            listView.adapter = adapter
            */
        }

        // 3 헌 어댑터 헌 데이터 -> notify 없이도 됨
        view.findViewById<Button>(R.id.OldOldButton).setOnClickListener {
            val new_list = listOf("fireEnblem", "Engage", "1", "20", "commingsoon")
            listData.clear()
            listData.addAll(new_list)
            listAdapter.notifyDataSetChanged()
        }
    }
}