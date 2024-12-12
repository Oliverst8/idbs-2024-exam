# Homework 1

## Q1 - 1The person relation contains 284 entries with a registered death date after ‘2010-02- 01’. How many entries do not have a registered death date?

### Part 1

```sql
SELECT count(*)
FROM person
WHERE deathdate > '2010-02-01';
```

### Part 2

```sql
SELECT count(*)
FROM person
WHERE deathdate IS NULL;
```

## Q2 - In the database, there are 46 movies in the French language for which the average height of all the people involved is greater than 185 centimeters (ignoring people with unregistered height). What is the number of movies in the Portuguese language for which the average height of all people involved is greater than 175 centimeters?

### Part 1

```sql
SELECT COUNT(*)
FROM (
    SELECT movieId, AVG(P.height)
    FROM Involved I
    JOIN person P ON I.personId = P.ID
    JOIN movie M ON I.movieId = M.id
    WHERE language = 'French'
    GROUP BY I.movieId
    HAVING AVG(P.height) > 185
) X;
```

### Part 2

```sql
SELECT COUNT(*)
FROM (
    SELECT movieId, AVG(P.height)
    FROM Involved I
    JOIN person P ON I.personId = P.ID
    JOIN movie M ON I.movieId = M.id
    WHERE language = 'Portuguese'
    GROUP BY I.movieId
    HAVING AVG(P.height) > 175
) X;
```

## Q3 - The movie genre relation does not have a primary key, which can lead to a movie having more than one entry with the same genre. As a result, there are 14 movies in movie genre that have the genre ‘Action’ assigned to them more than once. How many movies in movie genre have the genre ‘Thriller’ assigned to them more than once?

### Part 1

```sql
SELECT COUNT(*)
FROM (
    SELECT movieId
    FROM movie_genre
    WHERE genre = 'Action'  -- Specify the desired genre
    GROUP BY movieId
    HAVING COUNT(*) > 1
) X;
```

### Part 2

```sql
SELECT COUNT(*)
FROM (
    SELECT movieId
    FROM movie_genre
    WHERE genre = 'Thriller'  -- Specify the desired genre
    GROUP BY movieId
    HAVING COUNT(*) > 1
) X;
```

## Q4 - According to the information in the database, 52 different people acted in movies directed by ‘Ingmar Bergman’. How many different people acted in movies directed by ‘Akira Kurosawa’?

### Part 1

```sql
SELECT COUNT(DISTINCT I1.personId)
FROM involved I1
JOIN involved I2 ON I1.movieId = I2.movieId
JOIN person P ON I2.personId = P.id
WHERE I1.role = 'actor'
  AND I2.role = 'director'
  AND P.name = 'Ingmar Bergman';
```

### Part 2

```sql
SELECT COUNT(DISTINCT I1.personId)
FROM involved I1
JOIN involved I2 ON I1.movieId = I2.movieId
JOIN person P ON I2.personId = P.id
WHERE I1.role = 'actor'
  AND I2.role = 'director'
  AND P.name = 'Akira Kurosawa';
```

## Q5 - Of all the movies produced in 2007, there are 15 that have two directors involved in them. How many movies produced in 2010 have two directors involved in them?

### Part 1

```sql
SELECT COUNT(*)
FROM movie M
WHERE M.year = 2007
  AND M.id IN (
    SELECT I1.movieId
    FROM involved I1
    WHERE I1.role = 'director'
    GROUP BY I1.movieId
    HAVING COUNT(*) = 2
  );
```

### Part 2

```sql
SELECT COUNT(*)
FROM movie M
WHERE M.year = 2010
  AND M.id IN (
    SELECT I1.movieId
    FROM involved I1
    WHERE I1.role = 'director'
    GROUP BY I1.movieId
    HAVING COUNT(*) = 2
  );
```

## Q6 - There are 406 unique pairs of actors who have appeared together in exactly 10 movies released between 2000 and 2010. How many unique pairs of actors have appeared together in exactly 20 movies released between 2000 and 2010?

### Part 1

```sql
SELECT COUNT(*)
FROM (
    SELECT I1.personId AS actor1, I2.personId AS actor2, COUNT(*) AS movie_count
    FROM involved I1
    JOIN involved I2 ON I1.movieId = I2.movieId AND I1.personId < I2.personId
    JOIN movie M ON I1.movieId = M.id
    WHERE I1.role = 'actor' AND I2.role = 'actor' AND M.year BETWEEN 2000 AND 2010
    GROUP BY I1.personId, I2.personId
    HAVING COUNT(*) = 10
) X;
```

### Part 2

```sql
SELECT COUNT(*)
FROM (
    SELECT I1.personId AS actor1, I2.personId AS actor2, COUNT(*) AS movie_count
    FROM involved I1
    JOIN involved I2 ON I1.movieId = I2.movieId AND I1.personId < I2.personId
    JOIN movie M ON I1.movieId = M.id
    WHERE I1.role = 'actor' AND I2.role = 'actor' AND M.year BETWEEN 2000 AND 2010
    GROUP BY I1.personId, I2.personId
    HAVING COUNT(*) = 20
) X;
```

## Q7 - Of all the movies produced between 2000 and 2002, there are 782 that have entries registered in involved for all roles defined in the roles relation. How many movies produced between 2002 and 2004 have entries registered in involved for all roles defined in the roles relation? Note: This is a relational division query that must work for any instance; Do not use any ‘magic numbers’.

### Part 1

```sql
SELECT COUNT(*)
FROM (
    SELECT M.id
    FROM movie M
    JOIN involved I ON M.id = I.movieId
    WHERE M.year BETWEEN 2000 AND 2002
    GROUP BY M.id
    HAVING COUNT(DISTINCT I.role) = (
        SELECT COUNT(*)
        FROM role R
    )
) X;
```

### Part 2

```sql
SELECT COUNT(*)
FROM (
    SELECT M.id
    FROM movie M
    JOIN involved I ON M.id = I.movieId
    WHERE M.year BETWEEN 2002 AND 2004
    GROUP BY M.id
    HAVING COUNT(DISTINCT I.role) = (
        SELECT COUNT(*)
        FROM role R
    )
) X;
```

## Q8 - The number of people who have played a role in movies of all genres in the category ‘Newsworthy’ is 156. How many people have played a role in movies of all genres in the category ‘Newsworthy’ but have not played any role in movies that cover all genres in the category ‘Popular’?

### Part 1

```sql
SELECT COUNT(*)
FROM (
    SELECT I.personId
    FROM involved I
    JOIN movie_genre MG ON MG.movieId = I.movieId
    JOIN genre G ON MG.genre = G.genre
    WHERE G.category = 'Newsworthy'
    GROUP BY I.personId
    HAVING COUNT(DISTINCT G.genre) = (
        SELECT COUNT(*)
        FROM genre
        WHERE category = 'Newsworthy'
    )
) X;
```

### Part 2

```sql
SELECT COUNT(*)
FROM (
    SELECT I.personId
    FROM involved I
    JOIN movie_genre MG ON MG.movieId = I.movieId
    JOIN genre G ON MG.genre = G.genre
    WHERE G.category = 'Newsworthy'
    GROUP BY I.personId
    HAVING COUNT(DISTINCT G.genre) = (
        SELECT COUNT(*)
        FROM genre
        WHERE category = 'Newsworthy'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM involved I2
        JOIN movie_genre MG2 ON MG2.movieId = I2.movieId
        JOIN genre G2 ON MG2.genre = G2.genre
        WHERE I2.personId = I.personId
        AND G2.category = 'Popular'
        GROUP BY I2.personId
        HAVING COUNT(DISTINCT G2.genre) = (
            SELECT COUNT(*)
            FROM genre
            WHERE category = 'Popular'
        )
    )
) X;
```

# Homework 2

## Q1 - The empire ‘Great Britain’ consists of 4 countries. How many countries does the empire ‘Iberian’ consist of?

### Part 1

```sql
select count(*)
from empires E
where E.Empire = 'Great Britain';
```

### Part 2

```sql
select count(*)
from empires E
where E.Empire = 'Iberian';
```

## Q2 - There are 4 countries that are present on more than one continent. How many of these countries are partially in Asia?

### Part 1

```sql
select count(*)
from (
	select CC.CountryCode
	from countries_continents CC
	group by CC.CountryCode
	having count(*) > 1
) X;
```

### Part 2

```sql
select count(*)
from (
	select CC.CountryCode
	from countries_continents CC
		join countries_continents C2 on CC.CountryCode = C2.CountryCode
	where C2.Continent = 'Asia'
	group by CC.CountryCode
	having count(*) > 1
) X;
```

## Q3 - In the countries of North America that have more than 80 million inhabitants, there are a total of 111,946,176 people who speak Spanish, according to the statistics in the database. How many people who speak Spanish exist in the countries of Europe that have more than 50 million inhabitants?

### Part 1

```sql
select count(*)
from countries_continents
where percentage < 100 and continent = 'Asia';
```

### Part 2

```sql
select count(*)
from (
	select countrycode
	from countries_continents
	where continent = 'Asia'
	intersect
	select countrycode
	from countries_continents
	where continent <> 'Asia'
) X;
```

## Q4 - According to the database, two languages are spoken in all countries of ‘Benelux’. How many languages are spoken in all countries of ‘Danish Empire’?

_Note: This is a division query; points will only be awarded if division is attempted._

### Part 1

```sql
select sum(SpanishSpeaking)
from (
	select CO.Code, CO.Population * CL.Percentage / 100.0 as SpanishSpeaking
	from countries CO
		join countries_continents CC on CO.Code = CC.CountryCode
		join countries_languages CL on CO.Code = CL.CountryCode
	where CO.Population > 80000000
		and CC.Continent = 'North America'
		and CL.Language = 'Spanish'
) X;
```

### Part 2

```sql
select sum(SpanishSpeaking)
from (
	select CO.Code, CO.Population * CL.Percentage / 100.0 as SpanishSpeaking
	from countries CO
		join countries_continents CC on CO.Code = CC.CountryCode
		join countries_languages CL on CO.Code = CL.CountryCode
	where CO.Population > 50000000
		and CC.Continent = 'Europe'
		and CL.Language = 'Spanish'
) X;
```

# Homework 4

## Q1 - In the database, 353 songs have a duration of at least 10 minutes. What is the average duration of songs, in minutes, that have a duration between 5 and 25 minutes, inclusive? Round the number of minutes (ROUND(...)).

### Part 1

```sql
SELECT COUNT(*)
  FROM Songs
 WHERE Duration >= interval '10 minute';
```

### Part 2

```sql
SELECT ROUND(extract(EPOCH FROM AVG(Duration)) / 60) AS AverageDurationMinutes
FROM Songs
WHERE Duration >= interval '5 minute'
   AND Duration <= interval '25 minute';
```

## Q2 - What is the total duration in minutes of all explicit songs in the database? Round the number of minutes (ROUND(...)).

### Part 1

```sql
SELECT ROUND(extract(EPOCH FROM SUM(Duration))/60) AS Duration_in_hours
  FROM Songs
 WHERE IsExplicit = 1;
```

## Q3 - The database contains just 5 songs released in 1953. What is the average number of songs released in a year? Round the number of songs (ROUND(...)).

_Note: This is a very simple query. Try also to answer which year had the largest number of songs. Observe how much harder this query is!_

### Part 1

```sql
SELECT ROUND(AVG(x.cnt))
  FROM (
    SELECT COUNT(*) as cnt
      FROM Songs
     GROUP BY extract(YEAR FROM Releasedate)
  ) x;
```

### Part 2

```sql
SELECT extract(YEAR FROM Releasedate) as yr
  FROM Songs
 GROUP BY extract(YEAR FROM Releasedate)
HAVING COUNT(*) = (
    SELECT MAX(x.cnt)
      FROM (
        SELECT extract(YEAR FROM Releasedate) as yr, COUNT(*) as cnt
          FROM Songs
         GROUP BY extract(YEAR FROM Releasedate)
      ) x
    );
```

## Q4 - The database contains multiple albums by the artist Queen. Each album has a different average song duration, with the maximum average song duration of an album by Queen being 354 seconds. What is the maximum average song duration (in seconds) of an album by Miles Davis?

_Note: The output of the maximum average song duration is rounded ROUND(...)_

### Part 1

```sql
SELECT ROUND(extract(EPOCH FROM MAX(x.sd)))
  FROM (
    SELECT als.AlbumId, AVG(s.duration) sd
      FROM Artists a
      JOIN Songs s ON s.ArtistId = a.ArtistId
      JOIN AlbumSongs als ON als.SongId = s.SongId
     WHERE a.Artist = 'Queen'
     GROUP BY als.AlbumId
  ) x;
```

### Part 2

```sql
SELECT ROUND(extract(EPOCH FROM MAX(x.sd)))
  FROM (
    SELECT als.AlbumId, AVG(s.duration) sd
      FROM Artists a
      JOIN Songs s ON s.ArtistId = a.ArtistId
      JOIN AlbumSongs als ON als.SongId = s.SongId
     WHERE a.Artist = 'Miles Davis'
     GROUP BY als.AlbumId
  ) x;
```

## Q5 - There are 938 song titles that have been used for at least 2 songs, making up a total of 2072 songs with those titles. How many songs have a title that has been used for at least 4 songs?

### Part 1

```sql
SELECT SUM(C.cnt)
  FROM (
  SELECT COUNT(*) as cnt
    FROM Songs
   GROUP BY title
  HAVING COUNT(*) > 3
) C;

```

## Q6 - How many songs have been released after 2010 or belong to an album released in January.

### Part 1

```sql
SELECT COUNT(*)
  FROM (
    SELECT SongId
      FROM Songs s
     WHERE extract(year FROM s.Releasedate) > 2010
    UNION
    SELECT SongId
      FROM Albums al
      JOIN AlbumSongs als ON al.AlbumId = als.AlbumId
     WHERE extract(month FROM al.albumreleasedate) = 1
);
```

## Q7 - There are 1147 Albums with more than 1 song and none of them are Explicit. How many Albums consists of more than 1 song with all songs being Explicit?

### Part 1

```sql
SELECT COUNT(*)
FROM (
  SELECT COUNT(*)
    FROM AlbumSongs als
  GROUP BY als.AlbumId
  HAVING COUNT(als.SongId) = (
      SELECT COUNT(*)
        FROM AlbumSongs als2
        JOIN Songs s ON als2.SongId = s.SongId
      WHERE als2.AlbumId = als.AlbumId
        AND s.isExplicit = 1
  )  AND COUNT(*) > 1
);
```

## Q8 - The highest number of genres covered within an Album is 5. In the database, there is only one Album that has this amount of genres. What is the name of this Album?

_Note: Write your query to be capable of finding all albums that have the highest number of genres. (No hardcoded values)_

### Part 1

```sql
SELECT al.Album
  FROM Albums al
  JOIN AlbumGenres alg ON al.AlbumId = alg.AlbumId
 GROUP BY al.AlbumId, al.Album
HAVING COUNT(alg.GenreId) = (
    SELECT MAX(cnt) FROM (
        SELECT COUNT(*) as cnt
        FROM AlbumGenres
        GROUP BY AlbumId
    )
);
```

# Exam 2022

## a) - The chiffon fabric consists of 9 different elements. How many different elements does the cashmere fabric consist of?

### Part 1

```sql
select count (E.e_element)
from gFabrics F
	join gElements E on E.f_id = F.f_id
where F.f_name = 'chiffon';
```

### Part 2

```sql
select count (E.e_element)
from gFabrics F
	join gElements E on E.f_id = F.f_id
where F.f_name = 'cashmere'
```

## b) - There are 84 countries that have more than one designer. How many countries have more than two designers?

### Part 1

```sql
select count(*)
from (
select D.d_country, count(*)
from gDesigners D
group by D.d_country
having count(*) > 1
) X;
```

### Part 2

```sql
select count(*)
from (
select D.d_country, count(*)
from gDesigners D
group by D.d_country
having count(*) > 2
) X;
```

## c) - In the database, 12609 garments have a price that is higher than the average garmentprice. How many garments have a price that is lower than the average garment price?

### Part 1

```sql
select count(*)
from gGarments G
where G.g_Price < (select AVG(G.g_Price)
					from gGarments G);
```

## d) - How many garments with missing price values have a type of importance equal to six?

### Part 1

```sql
select count(*)
from gGarments gg
join gHasType gHT on gg.g_id = gHT.g_id
where gg.g_price IS NULL
and gHT.ht_importance = 6
```

## e) - How many main designers have designed garments in all categories that exist in the database? Note that in this query you should only consider the main designers, not co-designers.

### Part 1

```sql
select count(*)
from (
	select G.d_ID
	from gGarments G
		join gHasType HT on G.g_ID = HT.g_ID
		join gTypes T on HT.t_ID = T.t_ID
	group by G.d_ID
	having count(distinct T.t_category) = (
		select count(distinct T.t_category)
		from gTypes T
	)
) X;
```

## f) - The designer with d ID of 200 has collaborated, either as the main designer or as the co-designer, with 11 other designers from 7 different countries. How many designers have collaborated with other designers from 14 different countries?

### Part 1

```sql
select count(*)
from (
	select G.d_ID
	from gGarments G
		join gHasType HT on G.g_ID = HT.g_ID
		join gTypes T on HT.t_ID = T.t_ID
	group by G.d_ID
	having count(distinct T.t_category) = (
		select count(distinct T.t_category)
		from gTypes T
	)
) X;
```

# Exam 2023

## 11 - Speedy Express has already shipped 245 orders. How many orders have been shipped by Federal Shipping? NOTE: Orders' ship_via attribute is a FOREIGN KEY to Shippers. Write your SQL Query here:

### Part 1

```sql

```

### Part 2

```sql

```

## 13 - How many customers have received the highest discount? Write your SQL Query here:

### Part 1

```sql

```

## 15 - How many customers have no "Sales" in their contact title? Write your SQL Query here:

### Part 1

```sql

```

## 17 - How many customers have placed orders for products in all categories? Write your SQL query here:

### Part 1

```sql

```

# Exam 2024

## 11

100 different people have bought games that they have played.
How many different people have bought games that they have not played?

### Part 1

```sql
SELECT COUNT(DISTINCT b.pid)
FROM Buys b
LEFT JOIN Plays p ON b.pid = p.pid AND b.gid = p.gid
WHERE p.pid IS null;
```

## 13

How many people have played more games than they have bought?

### Part 1

```sql

```

## 15

There are 507 different people who have bought a game from a country that has a population of more than 20,000,000 people. How many different people have bought a game from a country that has a population of more than 30,000,000 people?

### Part 1

```sql
  SELECT DISTINCT(p.PID)
  FROM Person p
  JOIN Buys b ON b.PID = p.PID
  JOIN Game g ON g.GID = b.GID
  JOIN Developer d ON d.DID = g.DID
  JOIN Country c ON c.CID = d.CID
  WHERE c.population > 30000000;
```

## 17

There are 37 people who have bought some game from all developer companies that were founded in 1969. How many people have bought some game from all developer companies that were founded in 1960?

### Part 1

```sql
  SELECT DISTINCT p.pid, COUNT(DISTINCT d.did)
  FROM Person p
  JOIN Buys b ON b.PID = p.PID
  JOIN Game g ON g.GID = b.GID
  JOIN Developer d ON d.DID = g.DID
  WHERE d.since = 1960
  GROUP BY p.pid
  HAVING COUNT(DISTINCT d.did) = (
    SELECT COUNT(*)
    FROM Developer
    WHERE since = 1960
  );
```
