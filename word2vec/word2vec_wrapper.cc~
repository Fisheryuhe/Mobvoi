// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: jianwang@mobvoi.com (Jian Wang)
// Author: chhyu@mobvoi.com(Changhe Yu)

#include "util/nlp/word2vec/word2vec_wrapper.h"

#include<cstdio>
#include<cmath>

#include "base/log.h"
#include "base/string_util.h"

DEFINE_string(word2vec_model_file, "", "Word2vec model file path");

namespace word2vec {
// Max length of strings
static const long long max_size = 500;
// Number of closest words that will be shown
static const long long N = 40;
// Max length of vocabulary entries
static const long long max_w = 50;

struct Word2VecData {
  long long words, size;
  float *M;
  char *vocab;

  Word2VecData()
      : words(0UL),
        size(0UL),
        M(NULL),
        vocab(NULL) {
  }

  ~Word2VecData() {
    if (M) {
      free(M);
    }
    if (vocab) {
      free(vocab);
    }
  }
};

static void Fclose(FILE* file) {
  if (file) {
    fclose(file);
  }
}

Word2VecWrapper::Word2VecWrapper() {
  if (!Init(FLAGS_word2vec_model_file)) {
    LOG(FATAL) << "Fail to init word2vec from model file: "
               << FLAGS_word2vec_model_file;
  }
}

Word2VecWrapper::~Word2VecWrapper() {}

bool Word2VecWrapper::Init(const string& model_file) {
  std::unique_ptr<Word2VecData> word2_vec_data(new Word2VecData);
  std::unique_ptr<FILE, void (*)(FILE*)> model_file_ptr(
      fopen(model_file.c_str(), "rb"), Fclose);
  FILE* f = model_file_ptr.get();

  if (f == NULL) {
    LOG(ERROR) << "Fail to find word2vec model file:" << model_file;
    return false;
  }

  if (fscanf(f, "%lld", &word2_vec_data->words) == EOF) {
    LOG(ERROR) << "Fail to read num of words from word2vec model file:"
               << model_file;
    return false;
  }
  if (fscanf(f, "%lld", &word2_vec_data->size) == EOF) {
    LOG(ERROR) << "Fail to read size from word2vec model file:"
               << model_file;
    return false;
  }

  LOG(INFO) << "Init word2vec, words = " << word2_vec_data->words
            << ", size = " << word2_vec_data->size;

  word2_vec_data->vocab = (char *) malloc(
      (long long) (word2_vec_data->words) * max_w * sizeof(char));
  long long a, b;
  word2_vec_data->M = (float *) malloc(
      (long long) word2_vec_data->words * (long long) word2_vec_data->size
          * sizeof(float));
  if (!word2_vec_data->M) {
    LOG(ERROR)<< StringPrintf("Cannot allocate memory: %lld MB    %lld  %lld\n",
        (long long)word2_vec_data->words * word2_vec_data->size * sizeof(float) / 1048576,
        word2_vec_data->words, word2_vec_data->size);
    return false;
  }
  for (b = 0; b < word2_vec_data->words; b++) {
    a = 0;
    while (1) {
      word2_vec_data->vocab[b * max_w + a] = fgetc(f);
      if (feof(f) || (word2_vec_data->vocab[b * max_w + a] == ' '))
        break;
      if ((a < max_w) && (word2_vec_data->vocab[b * max_w + a] != '\n'))
        a++;
    }
    word2_vec_data->vocab[b * max_w + a] = 0;
    for (a = 0; a < word2_vec_data->size; a++) {
      if (fread(&word2_vec_data->M[a + b * word2_vec_data->size], sizeof(float),
                1, f) < 1) {
        LOG(ERROR) << "Fail to read vocab data from word2vec model file:"
                   << model_file << ", a = " << a << ", b = " << b;
        return false;
      }
    }
    float len = 0;
    for (a = 0; a < word2_vec_data->size; a++)
      len += word2_vec_data->M[a + b * word2_vec_data->size]
          * word2_vec_data->M[a + b * word2_vec_data->size];
    len = sqrt(len);
    for (a = 0; a < word2_vec_data->size; a++)
      word2_vec_data->M[a + b * word2_vec_data->size] /= len;
  }

  word2_vec_data_.swap(word2_vec_data);
  return true;
}

struct Bestw {
  char* bestw[N];

  Bestw() {
    for (long long a = 0; a < N; a++) {
      bestw[a] = (char *)malloc(max_size * sizeof(char));
      CHECK(bestw[a]);
      bestw[a][0] = 0;
    }
  }

  ~Bestw() {
    for (long long a = 0; a < N; a++) {
      free(bestw[a]);
    }
  }
};

bool Word2VecWrapper::TopN(const string& word,
                           vector<SemanticSimilarWord>* sim_word_vec) const {
  char st1[max_size];
  Bestw bw;
  char st[100][max_size];
  float dist, len, bestd[N], vec[max_size];
  long long a, b, c, d, cn, bi[100];

  for (a = 0; a < N; a++)
    bestd[a] = 0;

  strcpy(st1, word.c_str());
  a = b = c = cn = 0;
  while (1) {
    st[cn][b] = st1[c];
    b++;
    c++;
    st[cn][b] = 0;
    if (st1[c] == 0)
      break;
    if (st1[c] == ' ') {
      cn++;
      b = 0;
      c++;
    }
  }
  cn++;
  for (a = 0; a < cn; a++) {
    for (b = 0; b < word2_vec_data_->words; b++)
      if (!strcmp(&word2_vec_data_->vocab[b * max_w], st[a]))
        break;
    if (b == word2_vec_data_->words)
      b = -1;
    bi[a] = b;
    VLOG(3)
        << StringPrintf("Word: %s  Position in vocabulary: %lld", st[a], bi[a]);
    if (b == -1) {
      VLOG(4) << "Out of dictionary word: " << word;
      break;
    }
  }
  if (b == -1)
    return false;
  // Cosine distance
  for (a = 0; a < word2_vec_data_->size; a++)
    vec[a] = 0;
  for (b = 0; b < cn; b++) {
    if (bi[b] == -1)
      continue;
    for (a = 0; a < word2_vec_data_->size; a++)
      vec[a] += word2_vec_data_->M[a + bi[b] * word2_vec_data_->size];
  }
  len = 0;
  for (a = 0; a < word2_vec_data_->size; a++)
    len += vec[a] * vec[a];
  len = sqrt(len);
  for (a = 0; a < word2_vec_data_->size; a++)
    vec[a] /= len;
  for (a = 0; a < N; a++)
    bestd[a] = -1;
  for (c = 0; c < word2_vec_data_->words; c++) {
    a = 0;
    for (b = 0; b < cn; b++)
      if (bi[b] == c)
        a = 1;
    if (a == 1)
      continue;
    dist = 0;
    for (a = 0; a < word2_vec_data_->size; a++)
      dist += vec[a] * word2_vec_data_->M[a + c * word2_vec_data_->size];
    for (a = 0; a < N; a++) {
      if (dist > bestd[a]) {
        for (d = N - 1; d > a; d--) {
          bestd[d] = bestd[d - 1];
          strcpy(bw .bestw[d], bw.bestw[d - 1]);
        }
        bestd[a] = dist;
        strcpy(bw.bestw[a], &word2_vec_data_->vocab[c * max_w]);
        break;
      }
    }
  }
  for (a = 0; a < N; a++) {
    SemanticSimilarWord sim_word;
    sim_word.similar_word = bw.bestw[a];
    sim_word.cosine_dist = bestd[a];
    sim_word_vec->push_back(sim_word);
  }

  return true;
}

double Word2VecWrapper::WordDistance(const string& leftword, const string&  rightword) const {
  char st1[max_size];
  char st[100][max_size];
  float dist, len, vec[max_size];
  long long a, b, c, cn, bi[100];
  string tempstr = leftword + " " + rightword;
  strcpy(st1, tempstr.c_str());
  a = b = c = cn = 0;
  while (1) {
    st[cn][b] = st1[c];
    b++;
    c++;
    st[cn][b] = 0;
    if (st1[c] == 0)
      break;
    if (st1[c] == ' ') {
      cn++;
      b = 0;
      c++;
    }
  }
  cn++;
  for (a = 0; a < cn; a++) {
    for (b = 0; b < word2_vec_data_->words; b++)
      if (!strcmp(&word2_vec_data_->vocab[b * max_w], st[a]))
        break;
    if (b == word2_vec_data_->words)
      b = -1;
    bi[a] = b;
    VLOG(3)
        << StringPrintf("Word: %s  Position in vocabulary: %lld", st[a], bi[a]);
    printf("Word :%s Position in vocabulary:%lld\n",st[a],bi[a]);
    if (b == -1) {
      VLOG(4) << "Out of dictionary word: " << leftword;
      printf("Out of dictionary word:\n");
      break;
    }
  }
  if (b == -1)
    return false;
  // Cosine distance
  cn = 1;
  for (a = 0; a < word2_vec_data_->size; a++)
    vec[a] = 0;
  for (b = 0; b < cn; b++) {
    if (bi[b] == -1)
      continue;
    for (a = 0; a < word2_vec_data_->size; a++)
      vec[a] += word2_vec_data_->M[a + bi[b] * word2_vec_data_->size];
  }
  len = 0;
  for (a = 0; a < word2_vec_data_->size; a++)
    len += vec[a] * vec[a];
  len = sqrt(len);
  for (a = 0; a < word2_vec_data_->size; a++)
    vec[a] /= len;
    dist = 0;
    for (a = 0; a < word2_vec_data_->size; a++)
      dist += vec[a] * word2_vec_data_->M[a + bi[1] * word2_vec_data_->size];
   VLOG(5)
        << StringPrintf("\n                          word  ./Cosine distance\n ---------------------------------------------------\n%f\n",dist);
  printf("\n                          word  ./Cosine distance\n ---------------------------------------------------\n%f\n",dist);
 return dist;
 }
}  // namespace segmenter
