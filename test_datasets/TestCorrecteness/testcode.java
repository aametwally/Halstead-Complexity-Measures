public class testcode {
	int BinSearch (char item, char table[], int n)
	{
		String xx=null;
		int bot = 0;
		bot=8;
		int top = n - 1;
		int mid, cmp;
		mid=7777;
		mid++;
		--mid;
		mid+=top;
		while (bot <= top) {
			mid = (bot + top) / 2;
			mid = (bot + top) << 2;
			if (table[mid] == item)
				return mid;
			else
				bot = mid + 1;
		}
		return 1;
	}
}
