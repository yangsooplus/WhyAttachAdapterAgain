# WhyAttachAdapterAgain
Fragment의 ListView 데이터 갱신 후 Adapter를 다시 붙여야만 갱신이 된다는 글을 보고 의문을 가져 이유를 찾아보았다. 



-----------------------------------------------------


# 왜 Fragment ListView에 Adapter를 또 붙여야해?

### 문제의 블로그 포스팅

[안드로이드 ListView에 데이터 추가 또는 변경 시 갱신(Update)하기.](https://docko.tistory.com/273)
문제 시 블로그 포스팅 링크는 삭제하겠습니다. 


### 더 검색해 보았다

- 한국어 블로그에서는 딱 해결법만 적어둔 게시글이 2020~2021 정도에 몇 건
- 스택오버플로우에서는 2013~2019 쯤에 몇 가지 글이 있다.
    - 궁예하자면 아마 RecyclerView가 ListView를 점점 대체하기 시작하면서 해당 이슈 자체를 가지고 논박할 필요가 없어져서 그런 것이 아닐까 싶다. 
    (사실 아닌듯 - 미래의 양수진)

## 비슷한 케이스를 찾아봤다.

### Fragment가 아닌 상황에서 ListView가 갱신되지 않는 경우

[notifyDataSetChange not working from custom adapter](https://stackoverflow.com/questions/15422120/notifydatasetchange-not-working-from-custom-adapter)

**상황**

```java
public void updateReceiptsList(List<Receipt> newlist) {
        receiptlist = newlist;
        this.notifyDataSetChanged();
    }
```

→ ListView가 notifyDataSetChanged 호출 후에 새로운 데이터를 표시하지 않음

**작성자가 찾은 해결법**

```java
listview.setAdapter( new ReceiptListAdapter(activity,mcontext, 새 dataset);
```

새로운 데이터를 담은 어댑터 객체를 새로 만들어 리스트뷰에 set 해주었다.

하지만 왜 되는지는 모르겠다고 함

**답변**

```java
adapter = new CustomAdapter(data);
listview.setadapter(adapter);
```

adapter를 만들고 listview에 set 하면?

→ listview는 adapter가 hold하고 있는 object를 가리키는 상태가 된다

→ 즉 listview에 보여줄 data object(`data`) 를 가리킨다

글쓴이처럼 list 객체를 완전히 바꾸어버려서 다른 list 객체를 넣어버리면

→ 새로운 list 객체에 대한 정보를 모?름 (처음보는 애인데 얘가 바뀐건지 아닌지 알수가 없음)

혹은

```java
adapter = new CustomAdapter(anotherdata);
adapter.notifyDataSetChanged();
```

이렇게 다른 데이터인 `anotherdata`를 가진 새 어댑터 객체를 생성해도 똑같이 무반응일 것이다.

어댑터가 연결된 list에 대한 참조를 잃었기 때문에! 

**권장**

```java
public void updateReceiptsList(List<Receipt> newlist) {
    receiptlist.clear();
    receiptlist.addAll(newlist);
    this.notifyDataSetChanged();
}
```

이렇게 참조를 유지한 채로 데이터를 갈아끼는 식으로 가야한다.

### Fragment속 RecyclerView의 notifyDataSetChanged가 작동하지 않는 경우

[RecyclerView notifydatasetchanged not invoked from fragment](https://stackoverflow.com/questions/42038649/recyclerview-notifydatasetchanged-not-invoked-from-fragment)

[notifyDatasetChanged in RecyclerView not working from Fragment](https://stackoverflow.com/questions/34119275/notifydatasetchanged-in-recyclerview-not-working-from-fragment)

 여기서는 오히려 반대로 

```java
m_adapter.setData(items);

// (생략)

public void setData(List<PaffVocali> items)
{
    mItems = items;
}
```

이런식으로 Data를 adapter에 passing하는 것을 권장하고 있다. 

이것은 Fragment에서 데이터를 변경하지 않고 Adapter에서 데이터를 변경하는 것을 권장하는 것 같다.

→ 뭐지????? 여기서 엄청나게 혼란스러워졌다.

→ 저러면 원래 mItems 리스트의 참조를 잃어 갱신이 안되야 하는거 아냐???

다른 해결법으로는 계속 봤던 것처럼 어댑터를 다시 초기화 하라는 답변만 이유 없이 대고 있다…

```java
public void setUpdateArrayList(ArrayList<Updates> array){

        this.updateArrayList = array;

       ********** <Reinitialized the adapter here >**********
        this.recyclerView.getAdapter().notifyDataSetChanged();

    }
```

→ 게시글들을 종합해봐도 답변이 뒤죽박죽 하다…

그래서 직접 해보았다. 

```kotlin
// ----------------- Fragment onViewCreated------------------------
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.ListView)
        val listData = mutableListOf("hello", "boostcamp", "K026")
        val listAdapter = ListAdapter(requireContext(), listData)
        listView.adapter = listAdapter

        // 새 어댑터 새 리스트-> notify 없이도 됨
        // 처음 리스트뷰를 생성한 것 처럼 새 데이터로 만들어진 어댑터로 갈아 끼워버림
        view.findViewById<Button>(R.id.NewNewButton).setOnClickListener {
            val newObjListAdapter = ListAdapter(requireContext(), listOf("legend", "of", "Zelda", "TearOfKingdom"))
            listView.adapter = newObjListAdapter
        }

        // 헌 어댑터 새 리스트 -> 된다.
        view.findViewById<Button>(R.id.OldNewButton).setOnClickListener {
            listAdapter.changeDataListObject(listOf("kirby", "wii", "migration"))
            listAdapter.notifyDataSetChanged()
        }

        // 헌 어댑터 헌 리스트-> 된다.
        view.findViewById<Button>(R.id.OldOldButton).setOnClickListener {
            val new_list = listOf("fireEnblem", "Engage", "1", "20", "commingsoon")
            listData.clear()
            listData.addAll(new_list)
            listAdapter.notifyDataSetChanged()
        }
}

// ------------------ListAdater Class------------------------
fun changeDataListObject(new_data: List<String>) {
        data = new_data
    }
```

심지어 모 댓글에서 `notifyDataSetChanged` 를 `Adapter` 내부에서 호출했기 때문에 실행이 안된다는 말도 있었는데 무색하게 잘만 된다

```kotlin

// ----------------- Fragment onViewCreated------------------------
// 헌 어댑터 새 데이터 ->
  view.findViewById<Button>(R.id.OldNewButton).setOnClickListener {
      listAdapter.changeDataListObject(listOf("legend", "of", "Zelda", "TearOfKingdom"))
  }

// ------------------ListAdater Class------------------------
fun changeDataListObject(new_data: List<String>) {
        data = new_data
				this.notifyDataSetChanged()
    }
```

### 그럼 안된다던 사람들은 왜 안된거야?

유력한 후보는 

> fragment could be recreating so the adapter is recreating everytime the fragment recreate
> 

fragment가 재생성 될 때마다 어댑터가 새로 생성되어서 그런 것 이라는 가설이다.

### 나의 실험에는 맹점이 있었다

단 한번이 아닌 Fragment 이동, 여러번의 데이터 갱신 등의 액션을 취해봐야겠다고 생각하여

BottomNavigation, Fragment를 추가하고 매번 생성하던 listData와 listAdapter를 클래스 변수로 빼서 매번 생성하지 않도록 수정했다.

- 코드 전문
    
    ```kotlin
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
    
            // 새 어댑터 새 데이터 -> notify 없이도 됨
            // 처음 리스트뷰를 생성한 것 처럼 새 데이터로 만들어진 어댑터로 갈아 끼워버림
            view.findViewById<Button>(R.id.NewNewButton).setOnClickListener {
                val newObjListAdapter = ListAdapter(requireContext(), listOf("legend", "of", "Zelda", "TearOfKingdom"))
                listView.adapter = newObjListAdapter
            }
    
            // 헌 어댑터 새 데이터 
            view.findViewById<Button>(R.id.OldNewButton).setOnClickListener {
                listAdapter.changeDataListObject(listOf("kirby", "wii", "migration"))
                listAdapter.notifyDataSetChanged()
            }
    
            // 헌 어댑터 헌 데이터 
            view.findViewById<Button>(R.id.OldOldButton).setOnClickListener {
                val new_list = listOf("fireEnblem", "Engage", "1", "20", "commingsoon")
                listData.clear()
                listData.addAll(new_list)
                listAdapter.notifyDataSetChanged()
            }
        }
    }
    ```
    
![why1](https://user-images.githubusercontent.com/69582122/189985633-fe718173-3c80-4310-9bf5-e20c4b1fec99.gif)
![why2](https://user-images.githubusercontent.com/69582122/189985641-f053cee1-3319-4995-ba47-3409928eedc0.gif)
![why3](https://user-images.githubusercontent.com/69582122/189985647-90e2902e-c895-4833-a513-a1b3074d1ea8.gif)

**새 어댑터 새 리스트**

- [x]  첫 변경
- [x]  Fragment 이동 후 변경
    
    → Fragment 이동 하고 오면 데이터가 유지되지 않는다.
    
- [ ]  1→ 2 변경
- [x]  2 → 1 변경
- [ ]  1 → 3 변경
- [x]  3 → 1 변경

**헌 어댑터 새 리스트**

- [x]  첫 변경
- [x]  Fragment 이동 후 변경
    
     → Fragment 이동 하고 오면 데이터가 유지되지 않는다.
    
- [x]  2 → 1 변경
- [ ]  1 → 2 변경
- [ ]  2 → 3 변경
- [x]  3 → 2 변경

**헌 어댑터 헌 리스트**

- [x]  첫 변경
- [x]  Fragment 이동 후 변경
    
    → Fragment 이동 하고 오면 데이터가 유지
    
- [x]  3 → 1 변경
- [ ]  1 → 3 변경
- [x]  3 → 2 변경
- [ ]  2 → 3 변경

이외에도 여러번 마구 눌러보니 대충 갈피가 잡힌다

**새 어댑터 새 리스트**

→ 어댑터 객체 자체가 바뀌어 버렸기 때문에 초기화할 때 사용했던 어댑터를 잃어버렸음.

→ 잃어버린 어댑터에 대고 `notifyDataSetChanged` 을 해봤자 ListView에 적용될리 없음

→ 새 어댑터를 연결하는 것은 갱신되는 것처럼 보이겠지만, 사실은 계속 새로운 일회용 어댑터를 갖다 붙이고 있는 꼴이다

→ 그 근거는 `notifyDataSetChanged` 를 호출하지 않아도 ListView가 갱신되기 때문이다! 

→ 좋지 못하다!!!

**헌 어댑터 새 리스트**

→ 두가지 케이스로 나눌 수 있다. 

1. 어댑터 내부에서 새 리스트 객체로 바꾸는 것
2. 어댑터 밖에서 새 리스트 객체로 바꾸는 것

→ 1은 리스트가 갱신 되고, ~~2는 안 된다.~~ 다시 해보니까 된다?! 

→ 하지만 1도 좋은 방법은 아니다. Fragment 생명주기에 따라 영향을 받는다.

 `onViewCreated` 에서 ListView를 그리기 위해 어댑터를 붙이는 과정에서 새로운 어댑터가 붙게 되면 데이터가 보존되지 않는다.

→ ~2는 아예 어댑터가 리스트 객체가 바뀜을 알 수가 없다.~ 2도 갱신 된다. 갱신은 되지만 2 -> 3은 불가하다.
이 상태에서 데이터를 바꾸고 싶다면 아예 계속 List에 새 List를 붙여주거나 새로 붙인 List의 값을 바꿔야 한다.

### 결론

리스트뷰와 어댑터를 사용할 때, 참조를 잃지 않도록 유의하자!

아마 콕 집어 Fragment에서 notifyDataSetChanged()가 무반응이라고 호소하는 글들은 Fragment 생명주기 상 어댑터나 데이터의 참조를 잃어버린 케이스라고 생각된다.

가장 먼저 봤던 블로그의 해결 코드를 다시 보자. 

```kotlin
ArrayAdapter adapter = listView.getAdapter();
adapter.notifyDataSetChanged();
listView.setAdapter(adapter);
```

코틀린으로 바꾸어서 이렇게 써봤다. 

```kotlin
val new_list = mutableListOf("kirby", "wii", "migration")
listAdapter.changeDataListObject(new_list)

val adapter = listView.adapter as ListAdapter
//adapter.notifyDataSetChanged()
listView.adapter = adapter
```

이 코드, `notifyDataSetChanged` 를 실행시키기 위한 코드인데 `notifyDataSetChanged`를 빼도 같은 동작을 한다. 

사실 어댑터를 새로 달아주는 `listView.adapter = adapter` 요 라인을 실행할 때 어댑터의 `getView` 가 실행된다. 

```kotlin
override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.listview_item, null)
        view.findViewById<TextView>(R.id.textView).text = data[p0]
        Log.d("ListAdapter", "getView")
        return view
    }
```



여기서 끝이 아니다. 이렇게 해도 동작한다. 쇼킹~

```kotlin
val new_list = mutableListOf("kirby", "wii", "migration")
listAdapter.changeDataListObject(new_list)

val adapter = listView.adapter as ListAdapter
adapter.notifyDataSetChanged()
//listView.adapter = adapter
```

**어댑터를 다시 끼는 버전**

```kotlin
ArrayAdapter adapter = listView.getAdapter(); // 1
//adapter.notifyDataSetChanged();
listView.setAdapter(adapter); // 2
```

1. Fragment 전환 중 새로 생성되는 어댑터가 아닌 ListView에 붙어있던 초기 어댑터를 가져와서
2. 그 어댑터를 다시 달아줌으로서 어댑터가 `getView` 로 다시 UI를 그리도록 한다.
3. 리스트뷰를 싹 새로 그리는 것이기 때문에 data가 변했다면 변한 data로 나타날 것이다.

→ 실행시킬 때마다 ListView 전체 UI를 다시 그리고 있는 것이다!

→ 이거, `notifyDataSetChanged` 랑 완전히 똑같은 동작이다!!!!

 

사실 위에서 주구장창 파고들었지만… 일부의 변경만 알리는 방법이나 DiffUtil을 사용하는 것이 더 효율적이어서 선호되는 것은 알고 있다. 그래도 궁금증을 해결해서 개운하다 

아, 참조를 잃어버리는 문제는 리사이클러뷰에서도 동일하게 적용될 듯 하다.
