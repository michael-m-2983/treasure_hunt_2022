# Maze solving

### v0.1 - Manhattan Heuristic Selection + Dijkstra Pathfinding

Some especially suboptimal attempts:

![](/home/nifley/.config/marktext/images/2022-05-17-16-39-25-image.png)

![](/home/nifley/.config/marktext/images/2022-05-17-16-46-38-image.png)

Inefficient because its getting "closer" treasures that actually take much longer because of obstacles

```
Board Name     Score
_________________________

Level_0_0:     1,583
Level_0_1:     1,583
Level_0_2:     1,583
Level_0_3:     1,596
Level_0_4:     1,593
Level_1_0:     1,583
Level_1_1:     1,583
Level_1_2:     1,574
Level_2_0:     1,583
Level_2_1:     1,583
Level_2_2:     1,590
Level_2_3:     1,596
Level_3_0:     1,580
Level_3_1:     1,580
Level_3_10:    1,599
Level_3_2:     1,584
Level_3_3:     1,559
Level_3_4:     1,549
Level_3_5:     1,577
Level_3_6:     1,589
Level_3_7:     1,585
Level_3_8:     1,584
Level_3_9:     1,562
Level_4_0:     1,975
Level_4_1:     2,884
Level_4_2:    11,826
Level_5_0:     1,775
Level_5_1:     3,469
Level_5_2:     1,963
Level_5_3:     3,461
Level_6_0:     5,275
Level_6_1:     6,226
Level_6_2:     2,151
Level_6_3:     2,439
Level_6_4:     1,719
Level_6_5:     1,727
Level_6_6:     1,638
Level_7_0:     1,878
Level_7_1:     1,881
Level_7_2:     2,656
Level_8_0:     1,579
Level_8_1:     8,781

Grand Total: 101,681
```



### v0.1 - Actual Cost Selection + Dijkstra Pathfinding

Much better!!

![](/home/nifley/.config/marktext/images/2022-05-17-17-40-56-image.png)

![](/home/nifley/.config/marktext/images/2022-05-17-17-37-39-image.png)

```
Board Name     Score
_________________________

Level_0_0:     1,583
Level_0_1:     1,583
Level_0_2:     1,583
Level_0_3:     1,596
Level_0_4:     1,593
Level_1_0:     1,583
Level_1_1:     1,583
Level_1_2:     1,574
Level_2_0:     1,583
Level_2_1:     1,583
Level_2_2:     1,590
Level_2_3:     1,596
Level_3_0:     1,580
Level_3_1:     1,580
Level_3_10:    1,599
Level_3_2:     1,584
Level_3_3:     1,559
Level_3_4:     1,549
Level_3_5:     1,577
Level_3_6:     1,589
Level_3_7:     1,585
Level_3_8:     1,584
Level_3_9:     1,562
Level_4_0:     1,975
Level_4_1:     2,884
Level_4_2:    11,833
Level_5_0:     1,775
Level_5_1:     3,471
Level_5_2:     1,963
Level_5_3:     3,466
Level_6_0:     5,446
Level_6_1:     6,238
Level_6_2:     2,151
Level_6_3:     2,443
Level_6_4:     1,721
Level_6_5:     1,727
Level_6_6:     1,740
Level_7_0:     1,880
Level_7_1:     1,881
Level_7_2:     2,658
Level_8_0:     1,579
Level_8_1:     8,789

Grand Total: 101,998
```


